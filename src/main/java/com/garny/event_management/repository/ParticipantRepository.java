package com.garny.event_management.repository;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.Participant;
import com.garny.event_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByUser(User user);
    List<Participant> findByEvent(Event event);
    // Méthode de suppression par événement
    @Modifying
    @Query("DELETE FROM Participant p WHERE p.event = :event")
    void deleteParticipantsByEvent(@Param("event") Event event);
    void deleteByEvent(@Param("event") Event event);
    Optional<Participant> findByUserAndEvent(User user, Event event);
    List<Participant> findByEventAndStatus(Event event, Participant.ParticipantStatus status);
    long countByEventAndStatus(Event event, Participant.ParticipantStatus status);
}
