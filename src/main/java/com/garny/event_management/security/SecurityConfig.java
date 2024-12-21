package com.garny.event_management.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.garny.event_management.service.UserDetailsServiceImpl;


import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuration CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configuration CSRF
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .ignoringRequestMatchers(
                    
                    new AntPathRequestMatcher("/h2-console/**"), 
                    new AntPathRequestMatcher("/api/**")
                )
            )
            
            // Configuration des autorisations
            .authorizeHttpRequests(authz -> authz
                // Ressources publiques
                .requestMatchers(
                    "/", 
                    "/home", 
                    "/auth/login", 
                    "/auth/register", 
                    "/error", 
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/favicon.ico"
                ).permitAll()
                
                // Routes spécifiques aux rôles
                .requestMatchers("/dashboard/**").hasRole("ORGANIZER")
                .requestMatchers("/events/create").hasRole("ORGANIZER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/events/{id}").authenticated()
                .requestMatchers("/events/{id}/delete").authenticated()
                
                // Toutes les autres routes nécessitent une authentification
                .anyRequest().authenticated()
            )
            
            // Configuration de la connexion
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            
            // Configuration de la déconnexion
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
               .logoutSuccessUrl("/auth/login") 
                
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            )
            
            // Gestion des sessions
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true)
            )
            
            // Gestion des exceptions de sécurité
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")
            )
            
            // Configuration du fournisseur d'authentification
            .authenticationProvider(authenticationProvider());
        
        return http.build();
    }

    /**
     * Configuration CORS pour autoriser les requêtes cross-origin
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8080", 
            "https://votre-domaine.com"
        ));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
            "authorization", 
            "content-type", 
            "x-auth-token", 
            "X-CSRF-TOKEN"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "X-CSRF-TOKEN"
        ));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configuration du référentiel de tokens CSRF
     */
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-CSRF-TOKEN");
        repository.setParameterName("_csrf");
        return repository;
    }

    /**
     * Gestionnaire de succès d'authentification personnalisé
     */
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }
    /**
     * Fournisseur d'authentification
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Encodeur de mot de passe
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}