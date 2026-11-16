package com.citizentech.sos.security;

import com.citizentech.sos.exception.TooManyAttemptsException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/** Após 3 tentativas de login malsucedidas para um e-mail, novas tentativas são bloqueadas por 5 minutos. */
@Service
public class LoginAttemptService {
    private static final int MAX_FAILURES = 3;
    private static final Duration WINDOW = Duration.ofMinutes(5);

    private record Entry(int failures, Instant firstFailure) {}

    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();

    public void checkAllowed(String key) {
        Entry entry = entries.get(key);
        if (entry == null) return;
        if (Instant.now().isAfter(entry.firstFailure().plus(WINDOW))) {
            entries.remove(key);
            return;
        }
        if (entry.failures() >= MAX_FAILURES) {
            throw new TooManyAttemptsException("You failed 3 attempts. Try again in a few minutes.");
        }
    }

    public void recordFailure(String key) {
        entries.compute(key, (k, entry) -> {
            Instant now = Instant.now();
            if (entry == null || now.isAfter(entry.firstFailure().plus(WINDOW))) return new Entry(1, now);
            return new Entry(entry.failures() + 1, entry.firstFailure());
        });
    }

    public void reset(String key) { entries.remove(key); }
}
