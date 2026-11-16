package com.citizentech.sos.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class IotKeyService {
    private static final Logger log = LoggerFactory.getLogger(IotKeyService.class);

    private final byte[] key;

    public IotKeyService(@Value("${iot.api-key:}") String configured) {
        String value = configured;
        if (value == null || value.isBlank()) {
            byte[] random = new byte[32];
            new SecureRandom().nextBytes(random);
            value = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
            log.warn("IOT_API_KEY is not set. Key generated for this run (use it in the X-API-Key header): {}", value);
        }
        this.key = value.getBytes(StandardCharsets.UTF_8);
    }

    public boolean matches(String candidate) {
        return candidate != null && MessageDigest.isEqual(key, candidate.getBytes(StandardCharsets.UTF_8));
    }
}
