package com.g2bulk;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class TransportResponse {
    private final int status;
    private final Map<String, String> headers;
    private final byte[] body;

    public TransportResponse(int status, Map<String, String> headers, byte[] body) {
        this.status = status;
        Map<String, String> lower = new LinkedHashMap<>();
        if (headers != null) {
            for (Map.Entry<String, String> e : headers.entrySet()) lower.put(e.getKey().toLowerCase(Locale.ROOT), e.getValue());
        }
        this.headers = Collections.unmodifiableMap(lower);
        this.body = body == null ? new byte[0] : body;
    }

    public TransportResponse(int status, Map<String, String> headers, String body) {
        this(status, headers, body == null ? null : body.getBytes(StandardCharsets.UTF_8));
    }

    public int status() {
        return status;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public byte[] body() {
        return body;
    }
}
