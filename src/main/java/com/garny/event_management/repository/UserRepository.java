package com.garny.event_management.repository;
import com.garny.event_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.garny.event_management.entity.Event;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    // Nouvelle méthode pour trouver les événements d'un utilisateur
    @Query("SELECT DISTINCT e FROM Event e JOIN e.participants p WHERE p.user = :user")
    List<Event> findEventsByUser(@Param("user") User user);

    
    Optional<User> findByEmail(String email);
}

