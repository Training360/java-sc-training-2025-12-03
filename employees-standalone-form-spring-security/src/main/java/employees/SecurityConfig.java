package employees;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
//        return new Argon2PasswordEncoder(6, 256, 4, 65536, 4);// Kell a BouncyCastle függőség
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(registry ->
                        // Sorrend
                        // Specifikusabb szabályok kerüljenek felülre
                        // Automatizált teszteket írjunk rá!
                        registry.requestMatchers("/login")
                                .permitAll()
//                                .requestMatchers("/**") // EZ NAGYON ROSSZ, EZ EGY ELLENPÉLDA!
                                .requestMatchers("/", "/employees")
                                .hasRole("USER")
                                .requestMatchers("/create-employee")
                                .hasRole("ADMIN")
                                .anyRequest()
                                .denyAll()
                )
                .formLogin(Customizer.withDefaults())
                .logout(Customizer.withDefaults());
        return http.build();
    }
}
