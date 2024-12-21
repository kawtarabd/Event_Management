package com.garny.event_management.controller;

import com.garny.event_management.entity.Rating;
import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.User;
import com.garny.event_management.service.RatingService;
import com.garny.event_management.service.EventService;
import com.garny.event_management.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private EventService eventService;

    @GetMapping("/event/{eventId}")
    public String showEventRatings(@PathVariable Long eventId, Model model) {
        Event event = eventService.getEventById(eventId);
        List<Rating> ratings = ratingService.getEventRatings(event);
        double averageRating = ratingService.getEventAverageRating(event);

        model.addAttribute("event", event);
        model.addAttribute("ratings", ratings);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("newRating", new Rating());

        return "event-ratings";
    }

    @PostMapping("/event/{eventId}")
    public String addRating(@PathVariable Long eventId,
                          @Valid @ModelAttribute("newRating") Rating rating,
                          BindingResult result,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez corriger les erreurs dans le formulaire");
            return "redirect:/ratings/event/" + eventId;
        }

        try {
            Event event = eventService.getEventById(eventId);
            User user = userDetails.getUser();

            rating.setEvent(event);
            rating.setUser(user);

            ratingService.createRating(rating);
            redirectAttributes.addFlashAttribute("success", "Votre note a été ajoutée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/ratings/event/" + eventId;
    }

    @PostMapping("/{ratingId}/update")
    public String updateRating(@PathVariable Long ratingId,
                             @Valid @ModelAttribute Rating rating,
                             BindingResult result,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez corriger les erreurs dans le formulaire");
            return "redirect:/ratings/event/" + rating.getEvent().getId();
        }

        try {
            rating.setUser(userDetails.getUser());
            Rating updatedRating = ratingService.updateRating(ratingId, rating);
            redirectAttributes.addFlashAttribute("success", "Votre note a été mise à jour avec succès");
            return "redirect:/ratings/event/" + updatedRating.getEvent().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ratings/event/" + rating.getEvent().getId();
        }
    }

    @PostMapping("/{ratingId}/delete")
    public String deleteRating(@PathVariable Long ratingId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            Rating rating = ratingService.getRatingById(ratingId);
            Long eventId = rating.getEvent().getId();
            
            ratingService.deleteRating(ratingId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("success", "Votre note a été supprimée avec succès");
            
            return "redirect:/ratings/event/" + eventId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/events";
        }
    }

    @GetMapping("/user")
    public String showUserRatings(@AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        List<Rating> userRatings = ratingService.getUserRatings(userDetails.getUser());
        model.addAttribute("ratings", userRatings);
        return "user-ratings";
    }
}
