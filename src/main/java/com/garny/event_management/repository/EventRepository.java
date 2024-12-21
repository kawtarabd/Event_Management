package com.garny.event_management.repository;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByDateAfterOrderByDateAsc(LocalDateTime date);
    List<Event> findByDateBefore(LocalDateTime date);
    List<Event> findByOrderByDateDesc();
    List<Event> findByOrganizer(User organizer);
    
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Event> searchEvents(@Param("query") String query);
   
    List<Event> findByDateBetween(LocalDateTime start, LocalDateTime end);
}