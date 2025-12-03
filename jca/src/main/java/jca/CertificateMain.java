package jca;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class CertificateMain {

    public static void main(String[] args) throws Exception {
        // Bouncy Castle provider
        Security.addProvider(new BouncyCastleProvider());

        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var pair = generator.generateKeyPair();

        // Leíró információk
        // Név a X.500 szabvány szerint
        var name = new X500Name("CN=TrainingSelfSignedCertificate");
        var serialNumber = java.math.BigInteger.valueOf(System.currentTimeMillis());
        var start = new Date();
        var end = new Date(start.getTime() + Duration.ofDays(365).toMillis()); // Egy évig érvényes

        // Tanúsítvány
        var certificateBuilder = new JcaX509v3CertificateBuilder(name, serialNumber, start, end, name, pair.getPublic());

        // Aláírás
        var signer = new JcaContentSignerBuilder("SHA256withRSA").build(pair.getPrivate());
        var holder = certificateBuilder.build(signer);

        // X509 - itt térünk vissza standard Java-ba
        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(holder);

        // Olvasható, nem szabványos formátum
        System.out.println(certificate);

        // Írjuk ki DER formátumban (Windows - bináris):
        Files.write(Path.of("training-cert.der"), certificate.getEncoded());

        // Írjuk ki PEM formátumban (Linux - szöveges - Base64 + header/footer):
        // -----BEGIN CERTIFICATE-----
        try (var writer = new JcaPEMWriter(Files.newBufferedWriter(Path.of("training-cert.pem")))) {
            writer.writeObject(certificate);
        }

        // Privát kulcs DER formátumban
        Files.write(Path.of("training-private-key.der"), pair.getPrivate().getEncoded());

        // Privát kulcsot PEM formátum - jelszóval védhető
        // -----BEGIN ENCRYPTED PRIVATE KEY-----
        OutputEncryptor encryptor = new JceOpenSSLPKCS8EncryptorBuilder(
                PKCS8Generator.PBE_SHA1_3DES)
                .setRandom(new SecureRandom())
                .setPassword("secret".toCharArray())
                .build();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pair.getPrivate().getEncoded());
        PrivateKeyInfo pki = PrivateKeyInfo.getInstance(spec.getEncoded());
        PKCS8Generator gen = new PKCS8Generator(pki, encryptor);

        try (JcaPEMWriter writer = new JcaPEMWriter(new FileWriter("training-private-key.pem"))) {
            writer.writeObject(gen);
        }

    }
}
