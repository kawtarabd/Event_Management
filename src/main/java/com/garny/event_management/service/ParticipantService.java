package com.garny.event_management.service;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.Participant;
import com.garny.event_management.entity.User;
import com.garny.event_management.repository.ParticipantRepository;

import jakarta.persistence.Enumerated;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParticipantService {

    @Autowired
    private ParticipantRepository participantRepository;

    public List<Participant> getWaitlist(Event event) {
        return participantRepository.findByEventAndStatus(
            event, 
            Participant.ParticipantStatus.WAITLIST
        );}
    public List<Participant> findByUser(User user) {
        return participantRepository.findByUser(user);
    }
    public List<Participant> findByEventAndStatus(
        Event event, 
        Participant.ParticipantStatus status
    ) {
        return participantRepository.findByEventAndStatus(event, status);
    }
    @Transactional
    public Participant save(Participant participant) {
        return participantRepository.save(participant);
    }

    public List<Participant> findByEvent(Event event) {
        return participantRepository.findByEvent(event);
    }

    public boolean isUserRegisteredForEvent(User user, Event event) {
        return participantRepository.findByUserAndEvent(user, event).isPresent();
    }

    @Transactional
    public void deleteParticipant(Long id) {
        participantRepository.deleteById(id);
    }

    public List<Participant> findAll() {
        return participantRepository.findAll();
    }
}
