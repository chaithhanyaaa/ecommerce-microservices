package com.ecommerce.productservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        System.out.println("MY SECURITY CONFIG LOADED");

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)
                )


                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.GET, "/products/**")
                        .permitAll()

                        .requestMatchers(HttpMethod.POST, "/products")
                        .hasRole("SELLER")

                        .requestMatchers("/internal/**")
                        .authenticated()

                        .anyRequest()
                        .denyAll()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );



        return http.build();
    }
}