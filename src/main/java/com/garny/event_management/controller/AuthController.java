package com.garny.event_management.controller;

import com.garny.event_management.entity.User;
import com.garny.event_management.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal; // Ensure this import is present

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(
        Model model, 
        @RequestParam(required = false) String error,
        @RequestParam(required = false) String logout,
        Principal principal
    ) {
        if (principal != null) {
            // Get the authenticated user
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                // Redirect based on user role
                if (user.getRole() == User.Role.ORGANIZER) {
                    return "redirect:/dashboard/organizer"; // Redirect to organizer dashboard
                } else if (user.getRole() == User.Role.PARTICIPANT) {
                    return "redirect:/dashboard/participant"; // Redirect to participant dashboard
                }
            }
        }

        if (error != null) {
            model.addAttribute("error", "Identifiants invalides");
        }
        if (logout != null) {
            model.addAttribute("message", "Déconnexion réussie");
        }

        return "login";
    }
    
    @GetMapping("/register")
    public String registerPage(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/dashboard";
        }

        User user = new User();
        user.setRole(User.Role.PARTICIPANT);
        model.addAttribute("user", user);

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
        @Valid @ModelAttribute("user") User user, 
        BindingResult bindingResult, 
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
    
        try {
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                bindingResult.rejectValue("username", "error.user", "Ce nom d'utilisateur existe déjà");
                return "register";
            }
    
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                bindingResult.rejectValue("email", "error.user", "Cet email est déjà utilisé");
                return "register";
            }
    
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole(User.Role.PARTICIPANT);
    
            userRepository.save(user);
    
            return "redirect:/auth/login?success=true";
    
        } catch (Exception e) {
            model.addAttribute("error", "Une erreur est survenue lors de l'inscription");
            return "register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, 
                         HttpServletResponse response, 
                         Authentication authentication) {
        // Invalider la session
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        
        // Effacer le contexte de sécurité
        SecurityContextHolder.clearContext();
        
        // Rediriger vers la page de connexion
        return "redirect:/auth/login?logout=true";
    }
}