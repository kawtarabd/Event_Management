package com.garny.event_management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "user_id"})
})
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
   
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status = ParticipantStatus.CONFIRMED;

    @Column(nullable = false)
    private LocalDateTime registrationDate = LocalDateTime.now();

    private boolean hasPaid = false;
    private LocalDateTime paymentDate;
    private String paymentReference;

    private boolean hasAttended = false;
    private LocalDateTime checkInTime;

    private String specialRequirements;
    private boolean needsAccommodation = false;
    private boolean needsTransportation = false;

    public enum ParticipantStatus {
        CONFIRMED,
        WAITLIST,
        CANCELLED
    }

    // Constructors
    public Participant() {
        // Default constructor required by JPA
    }

    public Participant(User user, Event event) {
        this.user = user;
        this.event = event;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ParticipantStatus getStatus() {
        return status;
    }

    public void setStatus(ParticipantStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isHasPaid() {
        return hasPaid;
    }

    public void setHasPaid(boolean hasPaid) {
        this.hasPaid = hasPaid;
        if (hasPaid) {
            this.paymentDate = LocalDateTime.now();
        }
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public boolean isHasAttended() {
        return hasAttended;
    }

    public void setHasAttended(boolean hasAttended) {
        this.hasAttended = hasAttended;
        if (hasAttended) {
            this.checkInTime = LocalDateTime.now();
        }
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getSpecialRequirements() {
        return specialRequirements;
    }

    public void setSpecialRequirements(String specialRequirements) {
        this.specialRequirements = specialRequirements;
    }

    public boolean isNeedsAccommodation() {
        return needsAccommodation;
    }

    public void setNeedsAccommodation(boolean needsAccommodation) {
        this.needsAccommodation = needsAccommodation;
    }

    public boolean isNeedsTransportation() {
        return needsTransportation;
    }

    public void setNeedsTransportation(boolean needsTransportation) {
        this.needsTransportation = needsTransportation;
    }

    // Helper methods
    public boolean isConfirmed() {
        return status == ParticipantStatus.CONFIRMED;
    }

    public boolean isWaitlisted() {
        return status == ParticipantStatus.WAITLIST;
    }

    public boolean isCancelled() {
        return status == ParticipantStatus.CANCELLED;
    }

    public boolean canAttend() {
        return isConfirmed() && (event.isPast() || !event.isFullyBooked());
    }

    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    public String getName() {
        return user != null ? user.getUsername() : null;
    }

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
    }
}
