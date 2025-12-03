package jca;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
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

        // Írjuk ki PEM formátumban (Linux)
        try (var writer = new JcaPEMWriter(Files.newBufferedWriter(Path.of("training-cert.pem")))) {
            writer.writeObject(certificate);
        }

    }
}
