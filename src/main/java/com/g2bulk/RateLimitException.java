package com.g2bulk;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public class RateLimitException extends G2BulkException {
    private static final long serialVersionUID = 1L;

    public RateLimitException(String message, int status, Object body, Map<String, String> headers) {
        super(message, status, body, headers);
    }

    public Optional<Duration> retryAfter() {
        String value = headers().get("retry-after");
        if (value == null) return Optional.empty();
        try {
            double seconds = Double.parseDouble(value.trim());
            if (seconds < 0 || Double.isNaN(seconds) || Double.isInfinite(seconds)) return Optional.empty();
            return Optional.of(Duration.ofMillis((long) (seconds * 1000)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
