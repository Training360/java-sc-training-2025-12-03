package jca;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;

public class MacMain {

    public static void main(String[] args) throws Exception {
        var input = "Hello World!".getBytes(StandardCharsets.UTF_8);

        var keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        var key = keyGen.generateKey();

        var mac = Mac.getInstance("HmacSHA256");
        mac.init(key);

        var signature = mac.doFinal(input);

        var hex = HexFormat.of();
        System.out.println(hex.formatHex(signature));

        // Bob
        var bobSignature = mac.doFinal(input);
        System.out.println(hex.formatHex(bobSignature));

        // Nem használható kriptoban ez az összehasonlítás
        // var valid = Arrays.equals(signature, bobSignature);

        // Konstans idejű összehasonlítás
        var valid = MessageDigest.isEqual(signature, bobSignature);

        System.out.println("Valid: " + valid);
    }
}
