package com.garny.event_management.controller;

import com.garny.event_management.entity.Evaluation;
import com.garny.event_management.entity.Event;
import com.garny.event_management.service.EvaluationService;
import com.garny.event_management.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/evaluations")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private EventService eventService;

    // Display all evaluations for an event
    @GetMapping("/{eventId}")
    public String getEventEvaluations(@PathVariable Long eventId, Model model) {
        Event event = eventService.getEventById(eventId);
        model.addAttribute("event", event);
        model.addAttribute("evaluations", evaluationService.getEvaluationsByEvent(event));
        return "evaluations/list";
    }

    // Add a new evaluation
    @PostMapping("/{eventId}/add")
    public String addEvaluation(@PathVariable Long eventId, @RequestParam int rating,
                                 @RequestParam String comment, Model model) {
        evaluationService.addEvaluation(eventId, rating, comment);
        return "redirect:/evaluations/" + eventId;
    }
}
