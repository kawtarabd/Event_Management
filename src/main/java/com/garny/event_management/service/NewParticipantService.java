package com.garny.event_management.service;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.Participant;
import com.garny.event_management.entity.User;
import com.garny.event_management.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NewParticipantService {

    @Autowired
    private ParticipantRepository participantRepository;

    public List<Participant> findByUser(User user) {
        return participantRepository.findByUser(user);
    }

    public List<Participant> findByEvent(Event event) {
        return participantRepository.findByEvent(event);
    }

    public Optional<Participant> findByUserAndEvent(User user, Event event) {
        return participantRepository.findByUserAndEvent(user, event);
    }

    public Participant save(Participant participant) {
        return participantRepository.save(participant);
    }
}
