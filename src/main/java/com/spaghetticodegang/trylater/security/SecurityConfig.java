package com.spaghetticodegang.trylater.security;

import com.spaghetticodegang.trylater.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${cors.allowed-origin}")
    private String allowedOrigin;

    @Bean
    public JwtFilter jwtFilter(JwtService jwtService, UserDetailsService userDetailsService, AuthCookieService authCookieService) {
        return new JwtFilter(jwtService, authCookieService, userDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                // Keine Session → stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Autorisierung
                .authorizeHttpRequests(auth -> auth

                        // Öffentlich zugängliche Pfade
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/user").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // TODO: Für Entwicklung -> Für Produktiv entfernen
                        .requestMatchers("/h2-console/**").permitAll()

                        // Alle anderen müssen authentifiziert sein
                        .anyRequest().authenticated()
                )

                // HTTP Status 401 - Unauthorized senden
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )

                // Filter für Authentifizierung per Jwt
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Frame Options für H2
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )

                // CORS
                .cors(cors -> cors
                        .configurationSource(request -> {
                            var config = new org.springframework.web.cors.CorsConfiguration();
                            config.setAllowedOrigins(List.of(allowedOrigin));
                            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
                            config.setAllowCredentials(true);
                            config.setAllowedHeaders(List.of("Content-Type", "X-Requested-With", "Authorization"));
                            config.setExposedHeaders(List.of("Authorization"));
                            return config;
                        })
                );

        return http.build();
    }

}
