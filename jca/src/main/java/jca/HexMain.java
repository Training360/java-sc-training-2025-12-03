package jca;

import java.util.HexFormat;

public class HexMain {

    public static void main(String[] args) {
        var input = "Hello World!";
        var hex = HexFormat.of();
        var formatted = hex.formatHex(input.getBytes());
        System.out.println(formatted);
    }
}
