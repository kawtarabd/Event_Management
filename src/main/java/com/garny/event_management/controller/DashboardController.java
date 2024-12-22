package com.garny.event_management.controller;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.Participant;
import com.garny.event_management.entity.User;
import com.garny.event_management.service.EventService;
import com.garny.event_management.service.ParticipantService;
import com.garny.event_management.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.LinkedHashMap;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private ParticipantService participantService;

    // Classe interne pour les statistiques du participant
    public static class ParticipantStatistics {
        private int totalRegisteredEvents;
        private int upcomingEvents;
        private int pastEvents;
        private int waitlistedEvents;

        // Constructeur par défaut
        public ParticipantStatistics() {}

        // Getters et setters
        public int getTotalRegisteredEvents() {
            return totalRegisteredEvents;
        }

        public void setTotalRegisteredEvents(int totalRegisteredEvents) {
            this.totalRegisteredEvents = totalRegisteredEvents;
        }

        public int getUpcomingEvents() {
            return upcomingEvents;
        }

        public void setUpcomingEvents(int upcomingEvents) {
            this.upcomingEvents = upcomingEvents;
        }

        public int getPastEvents() {
            return pastEvents;
        }

        public void setPastEvents(int pastEvents) {
            this.pastEvents = pastEvents;
        }

        public int getWaitlistedEvents() {
            return waitlistedEvents;
        }

        public void setWaitlistedEvents(int waitlistedEvents) {
            this.waitlistedEvents = waitlistedEvents;
        }

        @Override
        public String toString() {
            return "ParticipantStatistics{" +
                "totalRegisteredEvents=" + totalRegisteredEvents +
                ", upcomingEvents=" + upcomingEvents +
                ", pastEvents=" + pastEvents +
                ", waitlistedEvents=" + waitlistedEvents +
                '}';
        }
    }

    @GetMapping
    public String showDashboard(
        @AuthenticationPrincipal CustomUserDetails currentUser, 
        Model model
    ) {
        try {
            if (currentUser == null) {
                logger.error("CustomUserDetails est null");
                model.addAttribute("errorMessage", "Utilisateur non authentifié");
                return "error";
            }

            logger.info("Tentative d'accès au dashboard pour l'utilisateur : {}", 
                currentUser.getUsername());
            
            User user = currentUser.getUser();
            if (user == null) {
                logger.error("User est null pour CustomUserDetails: {}", currentUser.getUsername());
                model.addAttribute("errorMessage", "Données utilisateur non trouvées");
                return "error";
            }

            if (user.getRole() == null) {
                logger.error("Rôle non défini pour l'utilisateur : {}", user.getUsername());
                model.addAttribute("errorMessage", "Rôle utilisateur non défini");
                return "error";
            }

            logger.info("Rôle de l'utilisateur : {}", user.getRole());

            // Gestion différenciée selon le rôle
            switch (user.getRole()) {
                case ORGANIZER:
                    return handleOrganizerDashboard(user, model);
                case PARTICIPANT:
                    return handleParticipantDashboard(user, model);
                default:
                    logger.error("Rôle non reconnu : {}", user.getRole());
                    model.addAttribute("errorMessage", "Rôle non reconnu: " + user.getRole());
                    return "error";
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du tableau de bord", e);
            logger.error("Stack trace complète:", e);
            model.addAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "error";
        }
    }

    private String handleOrganizerDashboard(User user, Model model) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            List<Event> organizerEvents = eventService.getEventsByOrganizer(user);
            if (organizerEvents == null) {
                organizerEvents = List.of(); // Empty list instead of null
            }
            logger.info("Nombre d'événements de l'organisateur : {}", organizerEvents.size());

            DashboardStatistics stats = calculateOrganizerStatistics(organizerEvents);
            logger.info("Statistiques calculées : {}", stats);

            Map<String, Long> eventTypeDistribution = calculateEventTypeDistribution(organizerEvents);
            logger.info("Distribution des types d'événements : {}", eventTypeDistribution);

            List<Event> upcomingEvents = getUpcomingEvents(organizerEvents);
            
            model.addAttribute("user", user);
            model.addAttribute("events", organizerEvents);
            model.addAttribute("upcomingEvents", upcomingEvents);
            model.addAttribute("stats", stats != null ? stats : createDefaultStatistics());
            model.addAttribute("eventTypeDistribution", eventTypeDistribution);
            
            return "participants/dashboard-organizer";
        } catch (Exception e) {
            logger.error("Erreur dans le dashboard organisateur pour l'utilisateur: {}", user.getUsername(), e);
            model.addAttribute("errorMessage", "Impossible de charger le tableau de bord organisateur: " + e.getMessage());
            return "error";
        }
    }

    private String handleParticipantDashboard(User user, Model model) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            logger.info("Récupération des participations pour l'utilisateur : {}", user.getUsername());
            List<Participant> participations = participantService.findByUser(user);
            logger.info("Nombre de participations trouvées : {}", 
            participations != null ? participations.size() : 0);

            // Gérer le cas où participations est null
            if (participations == null) {
                participations = List.of();
            }
            
            List<Event> participantEvents = participations.stream()
                .map(Participant::getEvent)
                .filter(event -> event != null)
                .collect(Collectors.toList());
            
            logger.info("Nombre d'événements du participant après filtrage : {}", participantEvents.size());

            logger.info("Calcul des événements à venir");
            List<Event> upcomingEvents = participantEvents.stream()
                .filter(event -> {
                    if (event == null) {
                        logger.warn("Événement null trouvé dans la liste");
                        return false;
                    }
                    if (event.getDate() == null) {
                        logger.warn("Date null trouvée pour l'événement : {}", event.getId());
                        return false;
                    }
                    return event.getDate().isAfter(LocalDateTime.now());
                })
                .sorted(Comparator.comparing(Event::getDate))
                .collect(Collectors.toList());
            logger.info("Nombre d'événements à venir : {}", upcomingEvents.size());

            logger.info("Calcul des statistiques");
            ParticipantStatistics stats = calculateParticipantStatistics(participations);
            logger.info("Statistiques calculées : {}", stats);

            logger.info("Ajout des attributs au modèle");

            model.addAttribute("user", user);
            model.addAttribute("events", participantEvents);
            model.addAttribute("upcomingEvents", upcomingEvents);
            model.addAttribute("stats", stats);
            
            return "participants/dashboard-participant";
        } catch (Exception e) {
            logger.error("Erreur dans le dashboard participant pour l'utilisateur: {}", user.getUsername(), e);
            model.addAttribute("errorMessage", "Impossible de charger le tableau de bord participant: " + e.getMessage());
            return "error";
        }
    }

    // Méthodes de calcul des statistiques

    private DashboardStatistics calculateOrganizerStatistics(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return createDefaultStatistics();
        }
    
        DashboardStatistics stats = new DashboardStatistics();
        
        stats.setTotalEvents(events.size());
        stats.setUpcomingEvents(
            events.stream()
                .filter(Event::isUpcoming)
                .collect(Collectors.toList()).size()
        );
        
        int totalParticipants = events.stream()
            .mapToInt(event -> event.getParticipants().size())
            .sum();
        stats.setTotalParticipants(totalParticipants);
    
        // Calcul du taux d'occupation moyen
        double occupancyRate = events.stream()
            .mapToDouble(Event::getOccupancyRate)
            .average()
            .orElse(0.0);
        stats.setOccupancyRate(occupancyRate);
        
        return stats;
    }

    private ParticipantStatistics calculateParticipantStatistics(List<Participant> participations) {
        if (participations == null || participations.isEmpty()) {
            return createDefaultParticipantStatistics();
        }

        ParticipantStatistics stats = new ParticipantStatistics();
        
        // Total des événements enregistrés
        stats.setTotalRegisteredEvents(participations.size());
        
        // Événements à venir
        long upcomingEventsCount = participations.stream()
            .map(Participant::getEvent)
            .filter(event -> event != null && event.isUpcoming())
            .count();
        stats.setUpcomingEvents((int) upcomingEventsCount);
        
        // Événements passés
        long pastEventsCount = participations.stream()
            .map(Participant::getEvent)
            .filter(event -> event != null && !event.isUpcoming())
            .count();
        stats.setPastEvents((int) pastEventsCount);
        
        // Événements en liste d'attente
        long waitlistedEventsCount = participations.stream()
            .filter(p -> p.getStatus() == Participant.ParticipantStatus.WAITLIST)
            .count();
        stats.setWaitlistedEvents((int) waitlistedEventsCount);
        
        return stats;
    }

    private ParticipantStatistics createDefaultParticipantStatistics() {
        ParticipantStatistics stats = new ParticipantStatistics();
        stats.setTotalRegisteredEvents(0);
        stats.setUpcomingEvents(0);
        stats.setPastEvents(0);
        stats.setWaitlistedEvents(0);
        return stats;
    }

    private Map<String, Long> calculateEventTypeDistribution(List<Event> events) {
        return events.stream()
            .collect(Collectors.groupingBy(Event::getType, Collectors.counting()));
    }

    private List<Event> getUpcomingEvents(List<Event> events) {
        return events.stream()
            .filter(Event::isUpcoming)
            .sorted(Comparator.comparing(Event::getDate))
            .collect(Collectors.toList());
    }

    private DashboardStatistics createDefaultStatistics() {
        DashboardStatistics stats = new DashboardStatistics();
        stats.setTotalEvents(0);
        stats.setUpcomingEvents(0);
        stats.setTotalParticipants(0);
        return stats;
    }

    public static class DashboardStatistics {
        private int totalEvents;
        private int upcomingEvents;
        private int totalParticipants;
        private double occupancyRate; // Nouveau champ
    
        // Ajoutez le getter et le setter pour occupancyRate
        public double getOccupancyRate() {
            return occupancyRate;
        }
    
        public void setOccupancyRate(double occupancyRate) {
            this.occupancyRate = occupancyRate;
        }

        // Getters et Setters
        public int getTotalEvents() {
            return totalEvents;
        }

        public void setTotalEvents(int totalEvents) {
            this.totalEvents = totalEvents;
        }

        public int getUpcomingEvents() {
            return upcomingEvents;
        }

        public void setUpcomingEvents(int upcomingEvents) {
            this.upcomingEvents = upcomingEvents;
        }

        public int getTotalParticipants() {
            return totalParticipants;
        }

        public void setTotalParticipants(int totalParticipants) {
            this.totalParticipants = totalParticipants;
        }

        @Override
        public String toString() {
            return "DashboardStatistics{" +
                "totalEvents=" + totalEvents +
                ", upcomingEvents=" + upcomingEvents +
                ", totalParticipants=" + totalParticipants +
                '}';
        }
    }
}