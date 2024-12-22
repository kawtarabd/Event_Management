package com.garny.event_management.repository;

import com.garny.event_management.entity.Rating;
import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByEventOrderByCreatedAtDesc(Event event);
    List<Rating> findByEvent(Event event);
    Optional<Rating> findByUserAndEvent(User user, Event event);
    List<Rating> findByUser(User user);
    void deleteByEvent(Event event);
    // Méthodes supplémentaires pour les statistiques
    long countByEvent(Event event);
    long countByEventAndScore(Event event, int score);
}
