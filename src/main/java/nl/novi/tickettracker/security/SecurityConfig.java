package nl.novi.tickettracker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUserSync jwtUserSync;

    public SecurityConfig(JwtUserSync jwtUserSync) {
        this.jwtUserSync = jwtUserSync;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Specifieke acties die alleen de Projectmanager mag doen
                        .requestMatchers(HttpMethod.POST, "/projects").hasRole("PROJECTMANAGER")

                        .requestMatchers(HttpMethod.PUT, "/projects/**").hasRole("PROJECTMANAGER")
                        .requestMatchers(HttpMethod.PUT, "/users/*/profile").hasRole("PROJECTMANAGER")
                        .requestMatchers(HttpMethod.PUT, "/tickets/{id}/assign").hasRole("PROJECTMANAGER")
                        .requestMatchers(HttpMethod.PUT, "/tickets/{id}/project").hasRole("PROJECTMANAGER")

                        .requestMatchers(HttpMethod.DELETE, "/tickets/{ticketId}/comments/{commentId}").hasAnyRole("PROJECTMANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/tickets/attachments/{attachmentId}").hasAnyRole("PROJECTMANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/tickets/{id}").hasRole("PROJECTMANAGER")

                        // Overige acties vereisen de rollen developer of projectmanager
                        .anyRequest().hasAnyRole("DEVELOPER", "PROJECTMANAGER")
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        // Aanroep voor gebruikerssynchronisatie via JWT-token met de database
        http.addFilterAfter(jwtUserSync, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            // Stap 1. Zoek in de token naar de map 'resource_access'
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess == null) return Collections.emptyList();

            // Stap 2. Zoek binnen de map naar de applicatie 'tickettracker'
            Object ticketTrackerMap = resourceAccess.get("tickettracker");
            if (!(ticketTrackerMap instanceof Map)) return Collections.emptyList();

            // Stap 3. Kopieer de lijst met rollen
            Object rolesList = ((Map<?, ?>) ticketTrackerMap).get("roles");
            if (!(rolesList instanceof List)) return Collections.emptyList();

            // Stap 4. Rollen omzetten naar het format uit de applicatie
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) rolesList;

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        });

        return converter;
    }
}