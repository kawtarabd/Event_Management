package com.garny.event_management.service;

import com.garny.event_management.entity.Rating;
import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.User;
import com.garny.event_management.repository.RatingRepository;
import com.garny.event_management.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

@Service
public class RatingService {
    @Autowired
    private RatingRepository ratingRepository;

    public Rating getRatingById(Long id) {
        return ratingRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Rating not found"));
    }

    public List<Rating> getUserRatings(User user) {
        return ratingRepository.findByUser(user);
    }

    @Transactional
    public Rating createRating(Rating rating) {
        validateRating(rating);
        return ratingRepository.save(rating);
    }

    @Transactional
    public Rating updateRating(Long id, Rating updatedRating) {
        Rating existingRating = getRatingById(id);
        
        // Vérifier que l'utilisateur est le propriétaire de la note
        if (!existingRating.getUser().equals(updatedRating.getUser())) {
            throw new BusinessException("Vous n'êtes pas autorisé à modifier cette note");
        }

        existingRating.setScore(updatedRating.getScore());
        existingRating.setComment(updatedRating.getComment());
        
        return ratingRepository.save(existingRating);
    }

    @Transactional
    public void deleteRating(Long id, User user) {
        Rating rating = getRatingById(id);
        
        if (!rating.getUser().equals(user)) {
            throw new BusinessException("Vous n'êtes pas autorisé à supprimer cette note");
        }

        ratingRepository.delete(rating);
    }

    public List<Rating> getEventRatings(Event event) {
        return ratingRepository.findByEventOrderByCreatedAtDesc(event);
    }

    public double getEventAverageRating(Event event) {
        OptionalDouble average = ratingRepository.findByEvent(event)
            .stream()
            .mapToInt(Rating::getScore)
            .average();
        
        return average.orElse(0.0);
    }

    public boolean hasUserRatedEvent(User user, Event event) {
        return ratingRepository.findByUserAndEvent(user, event).isPresent();
    }

    public long getEventRatingCount(Event event) {
        return ratingRepository.countByEvent(event);
    }

    public long getEventRatingCountByScore(Event event, int score) {
        return ratingRepository.countByEventAndScore(event, score);
    }

    private void validateRating(Rating rating) {
        // Vérifier si l'événement est terminé
        if (rating.getEvent().getDate().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Vous ne pouvez noter un événement qu'après sa fin");
        }

        // Vérifier si l'utilisateur a déjà noté cet événement
        if (hasUserRatedEvent(rating.getUser(), rating.getEvent())) {
            throw new BusinessException("Vous avez déjà noté cet événement");
        }

        // Vérifier si la note est dans la plage valide
        if (rating.getScore() < 1 || rating.getScore() > 5) {
            throw new BusinessException("La note doit être comprise entre 1 et 5");
        }

        // Vérifier la longueur du commentaire
        if (rating.getComment() != null && rating.getComment().length() > 500) {
            throw new BusinessException("Le commentaire ne peut pas dépasser 500 caractères");
        }
    }
}
