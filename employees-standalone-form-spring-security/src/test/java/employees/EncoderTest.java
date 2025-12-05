package employees;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class EncoderTest {

    @Test
    void encode() {
//        var encoder = new Argon2PasswordEncoder(6, 256, 4, 65536, 4);
//        var encoded = encoder.encode("user");
//        System.out.println(encoded);

        var encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("user"));
        System.out.println(encoder.encode("password"));

        //$2a$10$wwVY9.Jev4hUgBc195g2wetbFHgPtrUtxsrE5ffZ.ZYTvOh4j.9F6
        //$2a$10$13kw3IWbUlhOHEEv.zvZJemx1kqCv/zeM0DYDloEktdkbfO3DP3tm
    }
}
