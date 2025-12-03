package jca;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CertificateChainMain {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // Kulcspár generálása
    private static KeyPair generateRsaKeyPair(int bits) throws NoSuchAlgorithmException {
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
        kpGen.initialize(bits);
        return kpGen.generateKeyPair();
    }

    // X.509 tanúsítvány készítése (issuer aláírásával)
    private static X509Certificate generateCertificate(
            X500Name subject,
            PublicKey subjectPubKey,
            X500Name issuer,
            PublicKey issuerPubKey,
            PrivateKey issuerPrivKey,
            BigInteger serial,
            Date notBefore,
            Date notAfter,
            boolean isCA,
            int pathLenConstraint) throws Exception {

        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        csBuilder.setProvider("BC");
        ContentSigner signer = csBuilder.build(issuerPrivKey);

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serial,
                notBefore,
                notAfter,
                subject,
                subjectPubKey
        );

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

        // Subject Key Identifier - publikus kulcshoz tartozó egyedi azonosító - láncépítésnél segít
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(subjectPubKey));

        // Authority Key Identifier (issuer) - kibocsájtó publikus kulcshoz tartozó egyedi azonosító
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(issuerPubKey));


        // Key Usage
        if (isCA) {
            // Meghatározza, hogy CA-e
            // Nem a teljes lánc hosszát jelenti, hanem a CA-k mélységét alatta.
            if (pathLenConstraint >= 0) {
                certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(pathLenConstraint));
            }
            else {
                certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
            }
            // KeyUsage: aláírhat tanúsítványokat | aláírhat CRL-t
            certBuilder.addExtension(Extension.keyUsage, true,
                    new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
        } else {
            // Meghatározza, hogy CA-e
            certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
            // aláírás (pl. TLS handshake) | RSA key exchange (TLS-ben általános)
            certBuilder.addExtension(Extension.keyUsage, true,
                    new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
            // Extended Key Usage: TLS szerver
            certBuilder.addExtension(Extension.extendedKeyUsage, false,
                    new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
        }

        X509CertificateHolder holder = certBuilder.build(signer);
        JcaX509CertificateConverter conv = new JcaX509CertificateConverter().setProvider("BC");
        return conv.getCertificate(holder);
    }

    public static void main(String[] args) throws Exception {
        // Időablak
        Calendar cal = Calendar.getInstance();
        Date notBefore = cal.getTime();
        cal.add(Calendar.YEAR, 10);
        Date notAfter = cal.getTime();

        // 1) Root CA kulcs és önaláírt tanúsítvány
        KeyPair rootKp = generateRsaKeyPair(2048);
        X500Name rootName = new X500Name("CN=Example Root CA, O=MyOrg, C=HU");
        X509Certificate rootCert = generateCertificate(
                rootName, rootKp.getPublic(),
                rootName, rootKp.getPublic(), rootKp.getPrivate(),
                BigInteger.valueOf(System.currentTimeMillis()),
                notBefore, notAfter,
                true, // isCA
                1     // pathLenConstraint (1 intermediate)
        );

        // 2) Intermediate CA kulcs és tanúsítvány (aláírva a Root által)
        KeyPair interKp = generateRsaKeyPair(2048);
        X500Name interName = new X500Name("CN=Example Intermediate CA, O=MyOrg, C=HU");
        X509Certificate interCert = generateCertificate(
                interName, interKp.getPublic(),
                rootName, rootKp.getPublic(), rootKp.getPrivate(),
                BigInteger.valueOf(System.currentTimeMillis() + 1),
                notBefore, notAfter,
                true,
                0 // pathLenConstraint (no more sub-CAs)
        );

        // 3) End-entity (szerver) kulcs és tanúsítvány (aláírva Intermediate által)
        KeyPair serverKp = generateRsaKeyPair(2048);
        X500Name serverName = new X500Name("CN=www.example.com, O=MyOrg, C=HU");
        X509Certificate serverCert = generateCertificate(
                serverName, serverKp.getPublic(),
                interName, interKp.getPublic(), interKp.getPrivate(),
                BigInteger.valueOf(System.currentTimeMillis() + 2),
                notBefore, notAfter,
                false,
                -1
        );

        // Kiírjuk P7B fájlba, mert Windowsban a JKS-t csak importálni lehet, megjeleníteni nem
        // A PEM meg csak egy tanúsítványt képes tartalmazni
        // P12-őt is csak importálni tud
        List<Certificate> chain = Arrays.asList(serverCert, interCert, rootCert);

        JcaCertStore certStore = new JcaCertStore(chain);
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        gen.addCertificates(certStore);

        // 5. Üres tartalom (csak tanúsítványok, nincs aláírt adat)
        CMSTypedData data = new CMSProcessableByteArray(new byte[0]);
        CMSSignedData signedData = gen.generate(data, true); // encapsulate = true

        // 6. P7B fájl írása
        try (FileOutputStream fos = new FileOutputStream("chain.p7b")) {
            fos.write(signedData.getEncoded());
        }

        System.out.println("PKCS#7 lánc mentve: chain.p7b");
    }
}
