package com.example.eStore.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                                // ===== PUBLIC =====
                                .requestMatchers("/api/auth/**").permitAll()

                                // ===== PRODUCT =====
                                .requestMatchers(HttpMethod.GET, "/api/products/**")
                                .permitAll()

                                .requestMatchers(HttpMethod.POST, "/api/products/**")
                                .hasAnyRole("ADMIN", "STAFF")

                                .requestMatchers(HttpMethod.PUT, "/api/products/**")
                                .hasAnyRole("ADMIN", "STAFF")

                                .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                                .hasRole("ADMIN")

                                // ===== CATEGORY =====
                                .requestMatchers(HttpMethod.GET, "/api/categories/**")
                                .permitAll()

                                .requestMatchers("/api/categories/**")
                                .hasRole("ADMIN")

                                // ===== BRAND =====
                                .requestMatchers(HttpMethod.GET, "/api/brands/**")
                                .permitAll()

                                .requestMatchers("/api/brands/**")
                                .hasRole("ADMIN")

                                // ===== ADMIN =====
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                // ===== STAFF =====
                                .requestMatchers("/api/staff/**")
                                .hasAnyRole("STAFF", "ADMIN")

                                // ===== SHIPPER =====
                                .requestMatchers("/api/shipper/**")
                                .hasRole("SHIPPER")

                                // ===== CUSTOMER =====
                                .requestMatchers("/api/customer/**")
                                .hasRole("CUSTOMER")

                                // ===== ALL OTHER =====
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.sendError(401, "Unauthorized");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.sendError(403, "Forbidden");
                        }));


        return http.build();
    }
}
