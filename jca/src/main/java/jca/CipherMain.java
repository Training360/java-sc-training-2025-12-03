package jca;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Random;

public class CipherMain {

    public static void main(String[] args) throws Exception{
        var input = "Hello World!".getBytes(StandardCharsets.UTF_8);

        // Kulcsgenerálás
        var keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        var key = keyGenerator.generateKey();

        // IV - üzenetenként más és más
        byte[] iv = new byte[12];
        var random = SecureRandom.getInstanceStrong();
        random.nextBytes(iv);

        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        var spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        // Titkosítás
        var encrypted = cipher.doFinal(input);

        var hex = HexFormat.of();
        System.out.println(hex.formatHex(iv) + "." + hex.formatHex(encrypted));

        // Visszafejtés
        var decipher = Cipher.getInstance("AES/GCM/NoPadding");
        decipher.init(Cipher.DECRYPT_MODE, key, spec);
        var decrypted = decipher.doFinal(encrypted);
        System.out.println(new String(decrypted, StandardCharsets.UTF_8));
    }
}
