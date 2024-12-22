package com.garny.event_management.service;

import com.garny.event_management.entity.Event;
import com.garny.event_management.entity.User;
import com.garny.event_management.entity.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private JavaMailSender mailSender;

    // Notifications d'inscription
    public void sendRegistrationConfirmation(String to, Event event) {
        String subject = "Confirmation d'inscription - " + event.getTitle();
        String content = String.format(
            "Votre inscription à l'événement \"%s\" a été confirmée.\n\n" +
            "Date : %s\n" +
            "Lieu : %s\n\n" +
            "Merci de votre participation !",
            event.getTitle(),
            event.getDate().format(DATE_FORMATTER),
            event.getLocation()
        );
        sendEmail(to, subject, content);
    }

    public void sendWaitlistNotification(String to, Event event) {
        String subject = "Liste d'attente - " + event.getTitle();
        String content = String.format(
            "L'événement \"%s\" est actuellement complet.\n\n" +
            "Vous avez été ajouté à la liste d'attente. Nous vous contacterons si une place se libère.\n\n" +
            "Date : %s\n" +
            "Lieu : %s",
            event.getTitle(),
            event.getDate().format(DATE_FORMATTER),
            event.getLocation()
        );
        sendEmail(to, subject, content);
    }

    public void sendPromotionNotification(String to, Event event) {
        String subject = "Place disponible - " + event.getTitle();
        String content = String.format(
            "Une place s'est libérée pour l'événement \"%s\".\n\n" +
            "Votre inscription est maintenant confirmée !\n\n" +
            "Date : %s\n" +
            "Lieu : %s",
            event.getTitle(),
            event.getDate().format(DATE_FORMATTER),
            event.getLocation()
        );
        sendEmail(to, subject, content);
    }

    public void sendCancellationConfirmation(String to, Event event) {
        String subject = "Annulation d'inscription - " + event.getTitle();
        String content = String.format(
            "Votre inscription à l'événement \"%s\" a été annulée.\n\n" +
            "Date : %s\n" +
            "Lieu : %s\n\n" +
            "Si vous avez effectué un paiement, il sera remboursé sous 5 jours ouvrés.",
            event.getTitle(),
            event.getDate().format(DATE_FORMATTER),
            event.getLocation()
        );
        sendEmail(to, subject, content);
    }

    // Notifications de paiement
    public void sendPaymentConfirmation(String to, Event event, String transactionId) {
        String subject = "Confirmation de paiement - " + event.getTitle();
        String content = String.format(
            "Votre paiement pour l'événement \"%s\" a été confirmé.\n\n" +
            "ID de transaction : %s\n" +
            "Date : %s\n" +
            "Lieu : %s\n\n" +
            "Merci de votre confiance !",
            event.getTitle(),
            transactionId,
            event.getDate().format(DATE_FORMATTER),
            event.getLocation()
        );
        sendEmail(to, subject, content);
    }

    public void sendPaymentReminder(String to, Event event, Payment payment) {
        String subject = "Rappel de paiement - " + event.getTitle();
        String content = String.format(
            "Rappel : Le paiement pour l'événement \"%s\" est en attente.\n\n" +
            "Montant dû : %.2f €\n" +
            "Date limite : %s\n\n" +
            "Veuillez effectuer votre paiement pour confirmer votre participation.",
            event.getTitle(),
            payment.getAmount(),
            event.getDate().minusDays(7).format(DATE_FORMATTER)
        );
        sendEmail(to, subject, content);
    }

    // Rappels d'événements
    public void sendEventReminder(String to, Event event) {
        String subject = "Rappel d'événement - " + event.getTitle();
        String content = String.format(
            "Rappel : L'événement \"%s\" aura lieu prochainement.\n\n" +
            "Date : %s\n" +
            "Lieu : %s\n\n" +
            "N'oubliez pas votre confirmation d'inscription !",
            event.getTitle(),
            event.getDate().format(DATE_FORMATTER),
            event.getLocation()
        );
        sendEmail(to, subject, content);
    }

    // Notifications d'organisation
    public void sendEventCreationNotification(String to, Event event) {
        String subject = "Nouvel événement créé - " + event.getTitle();
        String content = String.format(
            "L'événement \"%s\" a été créé avec succès.\n\n" +
            "Date : %s\n" +
            "Lieu : %s\n" +
            "Capacité : %d personnes",
            event.getTitle(),
            event.getDate().format(DATE_FORMATTER),
            event.getLocation(),
            event.getCapacity()
        );
        sendEmail(to, subject, content);
    }

    public void sendEventUpdate(String to, Event event, String changes) {
        String subject = "Mise à jour de l'événement - " + event.getTitle();
        String content = String.format(
            "L'événement \"%s\" a été mis à jour.\n\n" +
            "Modifications :\n%s\n\n" +
            "Nouvelles informations :\n" +
            "Date : %s\n" +
            "Lieu : %s",
            event.getTitle(),
            changes,
            event.getDate().format(DATE_FORMATTER),
            event.getLocation()
        );
        sendEmail(to, subject, content);
    }

    public void sendEventCancellation(String to, Event event) {
        String subject = "Annulation d'événement - " + event.getTitle();
        String content = String.format(
            "L'événement \"%s\" a été annulé.\n\n" +
            "Date prévue : %s\n" +
            "Lieu : %s\n\n" +
            "Si vous avez effectué un paiement, il sera remboursé sous 5 jours ouvrés.",
            event.getTitle(),
            event.getDate().format(DATE_FORMATTER),
            event.getLocation()
        );
        sendEmail(to, subject, content);
    }

    // Méthode générique d'envoi d'email
    private void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            logger.info("Email envoyé à {} : {}", to, subject);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email à {} : {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }
}
