package nl.novi.tickettracker.security;

import nl.novi.tickettracker.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import java.io.IOException;

@Component
public class JwtUserSync extends OncePerRequestFilter {

    private final UserService userService;

    public JwtUserSync(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Controleren of er een geldig JWT-token van Keycloak aanwezig is
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String username = jwt.getClaimAsString("preferred_username");
            String email = jwt.getClaimAsString("email");

            if (username != null) {
                // Synchroniseren met database als user nog niet bestaat
                userService.provisionUserIfNeeded(username, email);
            }
        }

        filterChain.doFilter(request, response);
    }
}