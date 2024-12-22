package com.garny.event_management.controller;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.Participant;
import com.garny.event_management.entity.User;
import com.garny.event_management.security.CustomUserDetails;
import com.garny.event_management.service.EventService;
import com.garny.event_management.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/participants")
public class ParticipantDashboardController {

    @Autowired
    private EventService eventService;

    @Autowired
    private ParticipantService participantService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();

        // Get user's participations
        List<Participant> participations = participantService.findByUser(user);
        
        // Get all upcoming events
        List<Event> upcomingEvents = eventService.findUpcomingEvents();

        
        
        // Filter out events the user is already participating in
        List<Event> availableEvents = upcomingEvents.stream()
            .filter(event -> participations.stream()
                .noneMatch(p -> p.getEvent().getId().equals(event.getId())))
            .filter(event -> !event.isFullyBooked())
            .collect(Collectors.toList());

        // Calculate statistics
        long totalParticipants = participations.size();
        double occupancyRate = totalParticipants > 0 ? 
            (double) participations.stream()
                .filter(p -> p.getStatus() == Participant.ParticipantStatus.CONFIRMED)
                .count() / totalParticipants * 100 : 0;

        // Add attributes to model
        model.addAttribute("user", user);
        model.addAttribute("participations", participations);
        model.addAttribute("availableEvents", availableEvents);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("totalParticipants", totalParticipants);
        model.addAttribute("occupancyRate", occupancyRate);

        // Display success/error messages if they exist
        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.getAttribute("errorMessage"));
        }



          // Créer une liste de DTO pour les participations avec le nom de l'organisateur
    List<ParticipationDTO> participationsWithOrganizerName = participations.stream()
    .map(participation -> new ParticipationDTO(
        participation, 
        participation.getEvent().getOrganizer() != null 
            ? participation.getEvent().getOrganizer().getUsername() 
            : "Organisateur inconnu"
    ))
    .collect(Collectors.toList());
    // Remplacer l'attribut participations par participationsWithOrganizerName
    model.addAttribute("participations", participationsWithOrganizerName);

        return "participants/dashboard-participant";

      
    }
// Classe DTO pour transporter la participation avec le nom de l'organisateur
public static class ParticipationDTO {
    private Participant participation;
    private String organizerName;

    public ParticipationDTO(Participant participation, String organizerName) {
        this.participation = participation;
        this.organizerName = organizerName;
    }

    public Participant getParticipation() {
        return participation;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    // Méthodes déléguées pour faciliter l'utilisation dans le template
    public Event getEvent() {
        return participation.getEvent();
    }

    public Participant.ParticipantStatus getStatus() {
        return participation.getStatus();
    }


}}
