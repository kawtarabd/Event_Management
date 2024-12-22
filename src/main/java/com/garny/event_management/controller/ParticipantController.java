package com.garny.event_management.controller;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.Participant;
import com.garny.event_management.entity.User;
import com.garny.event_management.service.EventService;
import com.garny.event_management.security.CustomUserDetails;
import com.garny.event_management.service.NewParticipantService;
import com.garny.event_management.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/events")
public class ParticipantController {

    @Autowired
    private EventService eventService;

    @Autowired
    private NewParticipantService participantService;

    @Autowired
    private UserDetailsServiceImpl userService;

    @GetMapping("/{id}/participate")
    public String showPaymentPage(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        Event event = eventService.getEventById(id);
        
        if (event == null) {
            throw new RuntimeException("Event not found");
        }

        model.addAttribute("event", event);
        model.addAttribute("user", user);
        
        return "participants/payment";
    }

    @PostMapping("/payment/process")
    public String processPayment(
        @RequestParam Long eventId, 
        @RequestParam Double amount,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            Event event = eventService.getEventById(eventId);
            
            if (event == null) {
                throw new RuntimeException("Event not found");
            }

            // Process payment and create participation
            Participant participant = participantService.save(new Participant(user, event));

            redirectAttributes.addFlashAttribute("successMessage", 
                "Paiement réussi ! Vous êtes maintenant inscrit à l'événement.");
            
            return "redirect:/participants/dashboard";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erreur lors du traitement du paiement. Veuillez réessayer.");
            return "redirect:/events/" + eventId + "/participate";
        }
    }
}