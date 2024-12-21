package com.garny.event_management.service;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.User;
import com.garny.event_management.entity.Participant;
import com.garny.event_management.repository.EventRepository;
import com.garny.event_management.repository.ParticipantRepository;
import com.garny.event_management.repository.RatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {
    private static final Logger logger = LoggerFactory.getLogger(ParticipantService.class);
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private RatingRepository ratingRepository;

    public List<Participant> findByUser(User user) {
        return participantRepository.findByUser(user);
    }

  

    public List<Event> findUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findByDateAfterOrderByDateAsc(now);
    }

    @Transactional(readOnly = true)
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Événement non trouvé avec l'ID : " + id));
    }

    // Méthode pour sauvegarder un événement
    @Transactional
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        try {
              // Récupération de l'événement
              Event event = eventRepository.findById(eventId)
              .orElseThrow(() -> new EntityNotFoundException("Événement non trouvé"));

          // Suppression des participants
          participantRepository.deleteParticipantsByEvent(event);

          // Suppression des évaluations si nécessaire
          if (ratingRepository != null) {
              ratingRepository.deleteByEvent(event);
          }

          // Suppression de l'événement
          eventRepository.delete(event);

          logger.info("Événement supprimé avec succès - ID: {}", eventId);

      } catch (Exception e) {
          logger.error("Erreur lors de la suppression de l'événement", e);
          throw new RuntimeException("Impossible de supprimer l'événement", e);
      }
  
    }
            
    

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

   

    public List<Event> getEventsByOrganizer(User user) {
        return eventRepository.findByOrganizer(user);
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public void registerParticipant(Long eventId, User user) {
        // Logic to register a participant for the event
    }

// Méthode alternative pour récupérer les participants par utilisateur
public List<Participant> getParticipantsByUser(User user) {
    return participantRepository.findByUser(user);
}

    public Event updateEvent(Long eventId, Event event) {
        return eventRepository.save(event);
    }

    public List<Event> getEventsByType(String type) {
        // Logic to get events by type
        return null; // Placeholder return
    }

    public List<Event> searchEvents(String query) {
        return eventRepository.searchEvents(query);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}