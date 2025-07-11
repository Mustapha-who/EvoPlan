package evoplan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("ressources/static/success.html", "ressources/static/fail.html").permitAll()  // Autoriser les URLs de paiement
                        .anyRequest().authenticated()  // Exiger une authentification pour les autres routes
                )
                .csrf(csrf -> csrf.disable()); // Désactive CSRF si nécessaire

        return http.build();
    }
}
