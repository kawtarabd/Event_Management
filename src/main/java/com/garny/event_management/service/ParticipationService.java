package com.garny.event_management.service;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.Participant;
import com.garny.event_management.entity.User;
import com.garny.event_management.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional; 


@Service
public class ParticipationService {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private EventService eventService;

    @Transactional
    public Participant createParticipation(User user, Event event) {
        // Vérifier si l'utilisateur est déjà inscrit
        Optional<Participant> existingParticipant = 
            participantRepository.findByUserAndEvent(user, event);
        
        if (existingParticipant.isPresent()) {
            throw new RuntimeException("Vous êtes déjà inscrit à cet événement");
        }

        // Vérifier la capacité de l'événement
        if (event.isFullyBooked()) {
            // Ajouter à la liste d'attente
            Participant waitlistParticipant = new Participant(user, event);
            waitlistParticipant.setStatus(Participant.ParticipantStatus.WAITLIST);
            return participantRepository.save(waitlistParticipant);
        }

        // Créer un nouveau participant
        Participant participant = new Participant(user, event);
        participant.setStatus(Participant.ParticipantStatus.CONFIRMED);
        
        // Ajouter le participant à l'événement
        event.addParticipation(participant);
        
        // Sauvegarder l'événement et le participant
        eventService.saveEvent(event);
        return participantRepository.save(participant);
    }

    @Transactional
    public boolean processPayment(Long eventId, User user, Double amount) {
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }

        // Here you would typically integrate with a payment gateway
        // For now, we'll simulate a successful payment
        boolean paymentSuccessful = true;

        if (paymentSuccessful) {
            createParticipation(user, event);
            return true;
        }
        
        return false;
    }
}
