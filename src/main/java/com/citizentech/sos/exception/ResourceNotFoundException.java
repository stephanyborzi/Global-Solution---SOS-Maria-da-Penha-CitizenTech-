package com.citizentech.sos.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " " + id + " not found");
    }
}
