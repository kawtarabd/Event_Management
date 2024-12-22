package com.garny.event_management.controller;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.Participant;
import com.garny.event_management.entity.Rating;
import com.garny.event_management.entity.User;
import com.garny.event_management.repository.EventRepository;
import com.garny.event_management.service.EventService;
import com.garny.event_management.service.ParticipantService;
import com.garny.event_management.service.RatingService;
import com.garny.event_management.service.EmailService;
import com.garny.event_management.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;

@Controller
@RequestMapping("/events")
public class EventController {
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;


    @Autowired
    private EmailService emailService;

     @Autowired
    private RatingService ratingService;  

    @Autowired
    private ParticipantService participantService;  

    @GetMapping("/create")
    public String showCreateEventForm(Model model) {
        logger.info("Displaying event creation form");
        model.addAttribute("event", new Event());
        return "event-management-fixed";
    }

    @PostMapping("/create")
    public String createEvent(
        @Valid @ModelAttribute("event") Event event,
        BindingResult result,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            logger.warn("Validation errors during event creation");
            return "redirect:/dashboard"; 
        }
    
        try {
            validateEventCreation(event);
            event.setOrganizer(userDetails.getUser ());
            eventService.createEvent(event); // Le prix est inclus ici
            redirectAttributes.addFlashAttribute("success", "Événement créé avec succès");
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Error creating event", e);
            model.addAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/dashboard"; 
        }
    }
    
    private void validateEventCreation(Event event) {
        if (event.getDate() == null) {
            throw new IllegalArgumentException("La date de l'événement est obligatoire");
        }
        
        if (event.getDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de l'événement ne peut pas être dans le passé");
        }
        
        if (event.getCapacity() <= 0) {
            throw new IllegalArgumentException("La capacité doit être supérieure à zéro");
           
        }
        if (event.getPrice() < 0) {
            throw new IllegalArgumentException("Le prix doit être supérieur ou égal à zéro");
        }


    }

    @PostMapping("/{id}/register")
    public String registerForEvent(@PathVariable Long id,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            eventService.registerParticipant(id, userDetails.getUser ());
            redirectAttributes.addFlashAttribute("success", "Successfully registered for the event");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
        }
        return "redirect:/events/" + id;
    }

    @GetMapping("/{id}")
    public String getEventDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Tentative de récupération des détails de l'événement avec l'ID : {}", id);
        try {
        Event event = eventService.getEventById(id);
        User currentUser = userDetails.getUser();
        
        // Vérifications et attributs
        boolean isOrganizer = event.getOrganizer().getId().equals(currentUser.getId());
        boolean isUserRegistered = event.getParticipants().stream()
            .anyMatch(p -> p.getUser().getId().equals(currentUser.getId()));
        
        // Calcul des places disponibles
        int availableSpots = event.getCapacity() - event.getParticipants().size();
        
        // Gestion des évaluations
        List<Rating> ratings = ratingService.getEventRatings(event);
        double averageRating = ratingService.getEventAverageRating(event);
        boolean hasUserRated = ratingService.hasUserRatedEvent(currentUser, event);
        
        // Liste d'attente
        List<Participant> waitlist = participantService.getWaitlist(event);
        
        // Ajout des attributs au modèle
        model.addAttribute("event", event);
        model.addAttribute("isOrganizer", isOrganizer);
        model.addAttribute("isUserRegistered", isUserRegistered);
        model.addAttribute("availableSpots", availableSpots);
        model.addAttribute("ratings", ratings);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("hasUserRated", hasUserRated);
        model.addAttribute("waitlist", waitlist);
        
        return "details";
    } catch (Exception e) {
        logger.error("Erreur lors de la récupération des détails de l'événement", e);
        model.addAttribute("error", "Événement non trouvé");
        return "error";
    }
}


@GetMapping("/{id}/edit")
public String editEvent(@PathVariable Long id, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
    try {
        Event event = eventService.getEventById(id);
        if (!event.getOrganizer().getId().equals(userDetails.getUser().getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cet événement");
        }
        model.addAttribute("event", event);
        return "event-management";
    } catch (AccessDeniedException e) {
        logger.warn("Tentative non autorisée de modification d'événement", e);
        model.addAttribute("error", e.getMessage());
        return "error";
    } catch (Exception e) {
        logger.error("Erreur lors de la modification de l'événement", e);
        model.addAttribute("error", "Erreur lors de la récupération de l'événement");
        return "error";
    }
}
    

   
@PostMapping("/{id}/update")
public String updateEvent(
    @PathVariable Long id,
    @Valid @ModelAttribute("event") Event event,
    BindingResult result,
    @AuthenticationPrincipal CustomUserDetails userDetails,
    RedirectAttributes redirectAttributes,
    Model model
) {
    // Vérification des erreurs de validation
    if (result.hasErrors()) {
        logger.warn("Validation errors during event update");
        model.addAttribute("event", event); // Renvoyer l'événement au formulaire
        return "event-management"; // Retourne à la page de modification
    }

    try {
        Event existingEvent = eventService.getEventById(id);

        // Vérifier que seul l'organisateur peut modifier
        if (!existingEvent.getOrganizer().getId().equals(userDetails.getUser  ().getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cet événement");
        }

        // Mettre à jour les détails de l'événement
        existingEvent.setTitle(event.getTitle());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setDate(event.getDate());
        existingEvent.setLocation(event.getLocation());
        existingEvent.setCapacity(event.getCapacity());
        existingEvent.setType(event.getType());

        // Enregistrer les modifications
        eventService.updateEvent(id, existingEvent);

        redirectAttributes.addFlashAttribute("success", "Événement mis à jour avec succès");
        return "redirect:/dashboard"; // Redirige vers le tableau de bord

    } catch (AccessDeniedException e) {
        logger.warn("Tentative non autorisée de modification d'événement", e);
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/dashboard"; // Redirige vers le tableau de bord avec un message d'erreur
    } catch (Exception e) {
        logger.error("Erreur lors de la mise à jour de l'événement", e);
        redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour de l'événement");
        return "redirect:/dashboard"; // Redirige vers le tableau de bord avec un message d'erreur
    }
}



   @PostMapping("/{id}/delete")
    public String deleteEvent(
        @PathVariable Long id, 
        @AuthenticationPrincipal CustomUserDetails userDetails,
        RedirectAttributes redirectAttributes
    ) {
        try {
            // Log de débogage
            logger.info("Tentative de suppression de l'événement ID: {}", id);
            logger.info("Utilisateur connecté: {}", userDetails.getUsername());

            // Récupération de l'événement avec gestion d'erreur
            Event event = eventRepository.findById(id) // Utilisez l'instance injectée
                .orElseThrow(() -> new EntityNotFoundException("Événement non trouvé"));

            // Vérification des autorisations avec log détaillé
            User currentUser = userDetails.getUser();
            User eventOrganizer = event.getOrganizer();

            logger.debug("ID Organisateur de l'événement: {}", eventOrganizer.getId());
            logger.debug("ID Utilisateur connecté: {}", currentUser.getId());

            // Vérification des autorisations plus détaillée
            if (!eventOrganizer.getId().equals(currentUser.getId())) {
                logger.warn("Tentative de suppression non autorisée par l'utilisateur: {}", currentUser.getUsername());
                throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer cet événement");
            }

            // Suppression de l'événement avec gestion transactionnelle
            eventService.deleteEvent(id);

            // Log de succès
            logger.info("Événement supprimé avec succès - ID: {}, Organisateur: {}", 
                id, currentUser.getUsername());

            // Message de succès
            redirectAttributes.addFlashAttribute("success", "Événement supprimé avec succès");
            
            return "redirect:/dashboard";

        } catch (AccessDeniedException e) {
            // Gestion spécifique des accès non autorisés
            logger.warn("Accès non autorisé - Suppression d'événement", e);
            redirectAttributes.addFlashAttribute("error", "Vous n'avez pas les autorisations nécessaires.");
            return "redirect:/dashboard";

        } catch (EntityNotFoundException e) {
            // Gestion des événements non trouvés
            logger.error("Événement non trouvé lors de la suppression", e);
            redirectAttributes.addFlashAttribute("error", "L'événement n'existe pas ou a déjà été supprimé.");
            return "redirect:/dashboard";

        } catch (DataIntegrityViolationException e) {
            // Gestion des erreurs d'intégrité de données
            logger.error("Erreur d'intégrité lors de la suppression de l'événement", e);
            redirectAttributes.addFlashAttribute("error", 
                "Impossible de supprimer l'événement. Des dépendances existent peut-être.");
            return "redirect:/dashboard";

        } catch (Exception e) {
            // Gestion générique des exceptions
            logger.error("Erreur inattendue lors de la suppression de l'événement", e);
            redirectAttributes.addFlashAttribute("error", 
                "Une erreur est survenue lors de la suppression de l'événement.");
            return "redirect:/dashboard";
        }
    }
// Gestionnaire d'exceptions global amélioré
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(
        AccessDeniedException ex, 
        HttpServletRequest request,
        RedirectAttributes redirectAttributes
    ) {
        // Log détaillé
        logger.warn("Accès non autorisé - URL: {}", request.getRequestURI());
        logger.warn("Détails de l'erreur: {}", ex.getMessage());

        redirectAttributes.addFlashAttribute("error", 
            "Vous n'avez pas les autorisations nécessaires pour effectuer cette action.");
        
        return "redirect:/dashboard";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(
        Exception ex, 
        HttpServletRequest request,
        RedirectAttributes redirectAttributes
    ) {
        // Log complet de l'erreur
        logger.error("Erreur non gérée - URL: {}", request.getRequestURI(), ex);

        redirectAttributes.addFlashAttribute("error", 
            "Une erreur inattendue est survenue. Veuillez réessayer.");
        
        return "redirect:/dashboard";
    }
}

    @GetMapping
    public String listEvents(@RequestParam(required = false) String type,
                           @RequestParam(required = false) String search,
                           Model model) {
        List<Event> events;
        if (type != null && !type.isEmpty()) {
            events = eventService.getEventsByType(type);
        } else if (search != null && !search.isEmpty()) {
            events = eventService.searchEvents(search);
        } else {
            events = eventService.getAllEvents();
        }
        model.addAttribute("events", events);
        return "events/list";
    }

    @GetMapping("/my-events")
    public String myEvents(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser ();
        model.addAttribute("organizedEvents", eventService.getEventsByOrganizer(user));
        return "events/my-events";
    }
}