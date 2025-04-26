package com.spaghetticodegang.trylater.security;

import com.spaghetticodegang.trylater.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletResponse;
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

/**
 * Security configuration defining access rules, filters, and JWT-based authentication.
 */
@Configuration
public class SecurityConfig {

    /**
     * Creates the custom JWT filter used for authenticating requests via token.
     *
     * @param jwtService service for parsing and verifying JWTs
     * @param userDetailsService service for loading user data
     * @param authCookieService service for managing auth cookies
     * @return the configured JWT filter
     */
    @Bean
    public JwtFilter jwtFilter(JwtService jwtService, UserDetailsService userDetailsService, AuthCookieService authCookieService) {
        return new JwtFilter(jwtService, authCookieService, userDetailsService);
    }

    /**
     * Provides the Spring Security {@link AuthenticationManager}.
     *
     * @param config the authentication configuration provided by Spring
     * @return the authentication manager
     * @throws Exception if authentication setup fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures the password encoder used for hashing user passwords.
     *
     * @return the BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the main security filter chain, including access rules,
     * stateless session management, CORS, and custom JWT authentication.
     *
     * @param httpSecurity the security HTTP builder
     * @param jwtFilter the JWT authentication filter
     * @return the configured security filter chain
     * @throws Exception if the configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtFilter jwtFilter) throws Exception {
        httpSecurity
                    // Stateless session management
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    )

                    // Authorization rules
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.GET, "/", "/index.html", "/static/**", "/assets/**", "/favicon.ico").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/user").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                            .requestMatchers("/h2-console/**").permitAll()
                            .anyRequest().authenticated()
                    )

                    // Return 401 Unauthorized instead of redirecting
                    .exceptionHandling(ex -> ex
                            .authenticationEntryPoint((request, response, authException) -> {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                            })
                    )

                    // Register the JWT filter before Spring’s default auth filter
                    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                    // Disable unused default features
                    .csrf(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .logout(AbstractHttpConfigurer::disable)
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .cors(AbstractHttpConfigurer::disable)

                    // Enable H2 console frame support
                    .headers(headers ->
                            headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                    );

        return httpSecurity.build();
    }

}
