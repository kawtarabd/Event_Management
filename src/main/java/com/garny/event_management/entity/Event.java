package com.garny.event_management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDateTime date;
    private String location;
    private String description;
    private int capacity;
    private String type; // e.g., "conference", "workshop", "party"
    private double price;

    @OneToMany(mappedBy = "event", fetch = FetchType.EAGER)
    private Set<Participant> participants = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    // Method to add a participant
    public void addParticipation(Participant participant) {
        this.participants.add(participant);
        participant.setEvent(this);
    }

    // Helper methods
    public boolean isFullyBooked() {
        return participants.size() >= capacity;
    }

    public boolean hasParticipant(User user) {
        return participants.stream()
            .anyMatch(participant -> participant.getUser().equals(user));
    }

    public boolean isUpcoming() {
        return this.date != null && this.date.isAfter(LocalDateTime.now());
    }

    public boolean isPast() {
        return date.isBefore(LocalDateTime.now());
    }

    public double getOccupancyRate() {
        if (this.capacity <= 0) return 0.0;
        return (this.participants.size() * 100.0) / this.capacity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public Set<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Participant> participants) {
        this.participants = participants;
    }

    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
}
