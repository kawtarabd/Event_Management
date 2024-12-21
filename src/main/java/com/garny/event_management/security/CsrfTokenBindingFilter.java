package com.garny.event_management.security;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;



@Component
public class CsrfTokenBindingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenBindingFilter.class);
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        
        if (csrfToken == null) {
            logger.warn("CSRF token is missing for request: {}", request.getRequestURI());
        } else {
            // Add the token as a request attribute
            request.setAttribute(csrfToken.getParameterName(), csrfToken);
        }
        
        filterChain.doFilter(request, response);
    }
}