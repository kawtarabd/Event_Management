package com.garny.event_management.exception;

import org.springframework.web.bind.MethodArgumentNotValidException; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.access.AccessDeniedException;
import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessException(BusinessException ex, Model model) {
        logger.error("Erreur métier : {}", ex.getMessage());
        addErrorAttributes(model, HttpStatus.BAD_REQUEST, ex);
        return "error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        logger.warn("Ressource non trouvée : {}", ex.getMessage());
        addErrorAttributes(model, HttpStatus.NOT_FOUND, ex);
        return "error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(MethodArgumentNotValidException ex, Model model) {
        logger.warn("Validation errors occurred: {}", ex.getBindingResult().getAllErrors());
        addErrorAttributes(model, HttpStatus.BAD_REQUEST, ex);
        return "error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException ex, Model model) {
        logger.warn("Accès refusé : {}", ex.getMessage());
        model.addAttribute("error", "Vous n'avez pas les autorisations nécessaires.");
        return "error";
    }

  

    private void addErrorAttributes(Model model, HttpStatus status, Exception ex) {
        model.addAttribute("httpStatus", status.value());
        model.addAttribute("httpStatusReason", status.getReasonPhrase());
        model.addAttribute("errorMessage", ex.getMessage() != null ? ex.getMessage() : "Une erreur s'est produite");
        model.addAttribute("errorDetails", ex.toString());
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        model.addAttribute("stackTrace", sw.toString());
        
        // Add request timestamp
        model.addAttribute("timestamp", java.time.LocalDateTime.now());
        
        // Log the complete error details
        logger.error("Detailed error information:", ex);
    }
    
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        logger.error("Erreur inattendue", ex);
        model.addAttribute("error", "Une erreur est survenue : " + ex.getMessage());
        return "error";
    }
}