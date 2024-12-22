package com.garny.event_management.service;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.Participant;
import com.garny.event_management.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class EventReminderService {
    private static final Logger logger = LoggerFactory.getLogger(EventReminderService.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EmailService emailService;

    // Exécuter tous les jours à 9h00
    @Scheduled(cron = "0 0 9 * * *")
    public void sendEventReminders() {
        logger.info("Début de l'envoi des rappels d'événements");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxDate = now.plusDays(8); // Chercher les événements jusqu'à 8 jours à l'avance
        
        List<Event> upcomingEvents = eventRepository.findByDateBetween(now, maxDate);

        for (Event event : upcomingEvents) {
            if (shouldSendReminder(event, now)) {
                sendRemindersForEvent(event);
            }
        }
        logger.info("Fin de l'envoi des rappels d'événements");
    }

    // Exécuter toutes les heures pour les rappels de dernière minute
    @Scheduled(cron = "0 0 * * * *")
    public void sendLastMinuteReminders() {
        logger.info("Vérification des rappels de dernière minute");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.plusHours(1);
        
        List<Event> imminentEvents = eventRepository.findByDateBetween(now, nextHour);
        
        for (Event event : imminentEvents) {
            sendLastMinuteRemindersForEvent(event);
        }
    }

    private boolean shouldSendReminder(Event event, LocalDateTime now) {
        if (event.getDate() == null || event.getDate().isBefore(now)) {
            return false;
        }

        long daysUntilEvent = ChronoUnit.DAYS.between(now, event.getDate());
        return daysUntilEvent == 7 || daysUntilEvent == 3 || daysUntilEvent == 1;
    }

    private void sendRemindersForEvent(Event event) {
        for (Participant participant : event.getParticipants()) {
            try {
                emailService.sendEventReminder(participant.getUser().getEmail(), event);
                
                logger.info("Rappel envoyé à {} pour l'événement {}",
                    participant.getUser().getEmail(), event.getTitle());
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi du rappel à {} pour l'événement {}",
                    participant.getUser().getEmail(), event.getTitle(), e);
            }
        }
    }

    private void sendLastMinuteRemindersForEvent(Event event) {
        for (Participant participant : event.getParticipants()) {
            try {
                emailService.sendEventReminder(participant.getUser().getEmail(), event);
                
                logger.info("Rappel de dernière minute envoyé à {} pour l'événement {}",
                    participant.getUser().getEmail(), event.getTitle());
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi du rappel de dernière minute à {} pour l'événement {}",
                    participant.getUser().getEmail(), event.getTitle(), e);
            }
        }
    }
}
