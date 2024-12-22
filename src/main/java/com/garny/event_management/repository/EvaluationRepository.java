package com.garny.event_management.repository;

import com.garny.event_management.entity.Evaluation;
import com.garny.event_management.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByEvent(Event event);
}
