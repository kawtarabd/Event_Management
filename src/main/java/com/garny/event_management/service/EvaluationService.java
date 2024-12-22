package com.garny.event_management.service;

import com.garny.event_management.entity.Evaluation;
import com.garny.event_management.entity.Event;
import com.garny.event_management.repository.EvaluationRepository;
import com.garny.event_management.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationService {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private EventRepository eventRepository;

    // Get all evaluations for an event
    public List<Evaluation> getEvaluationsByEvent(Event event) {
        return evaluationRepository.findByEvent(event);
    }

    // Add a new evaluation
    public void addEvaluation(Long eventId, int rating, String comment) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        Evaluation evaluation = new Evaluation();
        evaluation.setEvent(event);
        evaluation.setRating(rating);
        evaluation.setComment(comment);
        evaluationRepository.save(evaluation);
    }
}
