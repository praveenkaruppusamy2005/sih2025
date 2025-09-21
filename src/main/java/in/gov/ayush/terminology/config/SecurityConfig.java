package in.gov.ayush.terminology.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Allow all requests for development - configure proper security in production
                .anyRequest().permitAll()
            );
            // Disable OAuth2 for development - enable in production
            // .oauth2ResourceServer(oauth2 -> oauth2
            //     .jwt(jwt -> jwt
            //         .decoder(jwtDecoder())
            //         .jwtAuthenticationConverter(jwtAuthenticationConverter())
            //     )
            // );

        return http.build();
    }

    // Disabled for development - enable in production
    // @Bean
    // public JwtDecoder jwtDecoder() {
    //     if (jwkSetUri != null && !jwkSetUri.isEmpty()) {
    //         return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    //     } else {
    //         return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    //     }
    // }

    // @Bean
    // public JwtAuthenticationConverter jwtAuthenticationConverter() {
    //     JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    //     authoritiesConverter.setAuthorityPrefix("ROLE_");
    //     authoritiesConverter.setAuthoritiesClaimName("authorities");

    //     JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    //     converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    //     return converter;
    // }

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;
}