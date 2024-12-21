package com.garny.event_management.service;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.Participant;
import com.garny.event_management.entity.User;
import com.garny.event_management.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    @Autowired
    private ParticipantRepository participantRepository;

    @Transactional
    public Participant processPaymentAndCreateParticipation(User user, Event event, Double amount) {
        // Here you would typically handle the actual payment processing
        // For now, we'll simulate a successful payment
        
        Participant participant = new Participant(user, event);
        participant.setStatus(Participant.ParticipantStatus.CONFIRMED);
        participant.setHasPaid(true);
        
        return participantRepository.save(participant);
    }
}
