package com.garny.event_management.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s non trouvé avec l'identifiant : %s", resourceName, identifier));
    }
}
