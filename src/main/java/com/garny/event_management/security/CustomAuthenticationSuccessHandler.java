package com.garny.event_management.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // Récupérer l'utilisateur authentifié
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        
        // Récupérer le rôle de l'utilisateur
        String role = user.getAuthorities().toString();

        // Logique de redirection selon le rôle de l'utilisateur
        if (role.contains("ROLE_PARTICIPANT")) {
            // Redirect participant to participant dashboard
            getRedirectStrategy().sendRedirect(request, response, "/participants/dashboard");
        } else if (role.contains("ROLE_ORGANIZER")) {
            // Redirect organizer to organizer dashboard
            getRedirectStrategy().sendRedirect(request, response, "/dashboard");
        } else {
            // Default redirect to home page
            getRedirectStrategy().sendRedirect(request, response, "/");
        }
    }
}

