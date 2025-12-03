package jca;

import java.util.Base64;

public class Base64Main {

    public static void main(String[] args) {
        var input = "Hello World!";
        var encoder = Base64.getEncoder();
        var encoded = encoder.encodeToString(input.getBytes());
        System.out.println(encoded);
    }
}
