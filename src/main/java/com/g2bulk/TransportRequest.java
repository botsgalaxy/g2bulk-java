package com.g2bulk;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TransportRequest {
    private final String method;
    private final URI uri;
    private final Map<String, String> headers;
    private final byte[] body;
    private final Duration timeout;

    public TransportRequest(String method, URI uri, Map<String, String> headers, byte[] body, Duration timeout) {
        this.method = method;
        this.uri = uri;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.body = body;
        this.timeout = timeout;
    }

    public String method() {
        return method;
    }

    public URI uri() {
        return uri;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String header(String name) {
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (e.getKey().equalsIgnoreCase(name)) return e.getValue();
        }
        return null;
    }

    public byte[] body() {
        return body;
    }

    public Duration timeout() {
        return timeout;
    }
}
