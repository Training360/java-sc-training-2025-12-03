package jca;

import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HexFormat;

public class KeyPairMain {

    public static void main(String[] args) throws Exception {
        // Kulcspár generálás
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048); // n, modulus mérete
        var pair = generator.generateKeyPair();

        // Privát kulcs
        PrivateKey privateKey = pair.getPrivate();

        var rsaPrivateKey = (java.security.interfaces.RSAPrivateKey) privateKey;

        System.out.println(privateKey);
        System.out.println(privateKey.getAlgorithm());
        System.out.println(privateKey.getFormat()); // PKCS#8

        //        PrivateKeyInfo ::= SEQUENCE {
//            version Version,
//            privateKeyAlgorithm PrivateKeyAlgorithmIdentifier,
//            privateKey PrivateKey,
//            attributes [0] IMPLICIT Attributes OPTIONAL
//        }


        System.out.println(rsaPrivateKey.getModulus());
        System.out.println(rsaPrivateKey.getPrivateExponent());

        var encodedPrivateKey = privateKey.getEncoded();

        var hex = HexFormat.of();
        System.out.println(hex.formatHex(encodedPrivateKey));

        // Publikus kulcs
        PublicKey publicKey = pair.getPublic();
        System.out.println(publicKey.getAlgorithm());
        System.out.println(publicKey.getFormat()); // X.509

        //        SubjectPublicKeyInfo ::= SEQUENCE {
//            algorithm AlgorithmIdentifier,
//            subjectPublicKey BIT STRING
//        }


    }
}
