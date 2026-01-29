package com.tricol.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.tricol.entities.UserApp;
import com.tricol.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtService jwtService;
    
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.error("Access denied: {}", accessDeniedException.getMessage());
                            jwtAuthenticationEntryPoint.commence(request, response, 
                                new org.springframework.security.authentication.BadCredentialsException(
                                    "Access denied: " + accessDeniedException.getMessage(), accessDeniedException));
                        })
                        .bearerTokenResolver(req -> {
                            String header = req.getHeader("Authorization");
                            if (header == null || !header.startsWith("Bearer ")) {
                                log.debug("No Authorization header or not Bearer token");
                                return null;
                            }
                            String token = header.substring(7);

                            // Check if it's a local JWT token (our own tokens)
                            // If jwtService can extract username, it's a local token -> let
                            // JwtAuthenticationFilter handle it
                            try {
                                jwtService.extractUsername(token);
                                // Local JWT detected - return null so OAuth2 resource server skips it
                                // and JwtAuthenticationFilter processes it instead
                                log.debug("Local JWT detected - delegating to JwtAuthenticationFilter");
                                return null;
                            } catch (Exception e) {
                                // Not a local JWT (likely a Keycloak token) -> pass to OAuth2 resource server
                                log.debug("Non-local JWT detected (Keycloak?) - passing to OAuth2 resource server: {}", e.getMessage());
                                return token;
                            }
                        })
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(keycloakJwtAuthConverter())))
                .authenticationProvider(authenticationProvider())

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter keycloakJwtAuthConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            try {
                String keycloakId = jwt.getSubject();
                String username = jwt.getClaimAsString("preferred_username");
                log.info("Keycloak JWT converter - keycloakId: {}, username: {}", keycloakId, username);

                if (username == null || username.isBlank()) {
                    log.error("preferred_username claim is missing or empty in Keycloak token");
                    return new ArrayList<>();
                }

                UserApp user = userService.findOrCreateKeycloakUser(keycloakId, username);
                log.info("Found/created user: {} with role: {}", user.getUsername(), user.getRole() != null ? user.getRole().getName() : "null");
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
                log.info("Loaded authorities for Keycloak user: {}", userDetails.getAuthorities());

                return new ArrayList<GrantedAuthority>(userDetails.getAuthorities());
            } catch (Exception e) {
                log.error("Error in Keycloak JWT converter: {}", e.getMessage(), e);
                return new ArrayList<>();
            }
        });
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * Custom JwtDecoder that validates the token signature using JWKS
     * but skips issuer validation to allow tokens issued by localhost:8081
     * to be validated when running inside Docker (where Keycloak is at tricol-keycloak:8080)
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        
        // Only validate timestamp (expiration), skip issuer validation
        // This allows tokens with issuer "localhost:8081" to work inside Docker
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator()
        );
        decoder.setJwtValidator(validator);
        
        log.info("JwtDecoder configured with JWK Set URI: {} (issuer validation disabled for Docker compatibility)", jwkSetUri);
        return decoder;
    }
}
