package com.garny.event_management.repository;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.Payment;
import com.garny.event_management.entity.PaymentStatus;
import com.garny.event_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUser(User user);
    List<Payment> findByEvent(Event event);
    List<Payment> findByStatus(PaymentStatus status);
    Optional<Payment> findByUserAndEventAndStatus(User user, Event event, PaymentStatus status);
    List<Payment> findByUserAndStatus(User user, PaymentStatus status);
    List<Payment> findByEventAndStatus(Event event, PaymentStatus status);
}
