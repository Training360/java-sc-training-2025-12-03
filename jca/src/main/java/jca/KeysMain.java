package jca;

import javax.crypto.KeyGenerator;
import java.util.HexFormat;

public class KeysMain {

    public static void main(String[] args) throws Exception {
        var keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256); // Alapbó erős random, de paraméterezhető
        var secretKey = keyGenerator.generateKey();

        var hex = HexFormat.of();
        System.out.println(hex.formatHex(secretKey.getEncoded()));
    }
}
