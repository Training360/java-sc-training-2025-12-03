package jca;

import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

public class HashMain {

    public static void main(String[] args) throws Exception {
        var input = "Hello World!".getBytes(StandardCharsets.UTF_8);
//        var input = Files.readAllBytes(Path.of("data.bin"));
        var digest = MessageDigest.getInstance("SHA-256");
        var hash = digest.digest(input);

        var hex = HexFormat.of();
        System.out.println(hex.formatHex(hash));

    }
}
