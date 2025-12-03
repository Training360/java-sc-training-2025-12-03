package jca;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.HexFormat;

public class SignMain {

    public static void main(String[] args) throws Exception {
        var data = "Hello World!".getBytes();

        var keyStore = KeyStore.getInstance("PKCS12");

        try (var input = Files.newInputStream(Path.of("training-keystore.p12"))) {
            keyStore.load(input, "secret".toCharArray());
        }

        var privateKey = (PrivateKey) keyStore.getKey("training", "secret".toCharArray());

        var signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        var signatureBytes = signature.sign();

        var hex = HexFormat.of();
        System.out.println(hex.formatHex(signatureBytes));
    }
}
