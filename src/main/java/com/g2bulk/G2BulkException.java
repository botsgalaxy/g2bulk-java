package com.g2bulk;

import java.util.Collections;
import java.util.Map;

public class G2BulkException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int status;
    private final transient Object body;
    private final Map<String, String> headers;

    public G2BulkException(String message) {
        this(message, 0, null, null, null);
    }

    public G2BulkException(String message, Throwable cause) {
        this(message, 0, null, null, cause);
    }

    public G2BulkException(String message, int status, Object body, Map<String, String> headers) {
        this(message, status, body, headers, null);
    }

    G2BulkException(String message, int status, Object body, Map<String, String> headers, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.body = body;
        this.headers = headers == null ? Collections.emptyMap() : Collections.unmodifiableMap(headers);
    }

    public int status() {
        return status;
    }

    public Object body() {
        return body;
    }

    public Map<String, String> headers() {
        return headers;
    }

    static G2BulkException forStatus(int status, String message, Object body, Map<String, String> headers) {
        if (status == 400) return new BadRequestException(message, status, body, headers);
        if (status == 401) return new AuthenticationException(message, status, body, headers);
        if (status == 403) return new PermissionDeniedException(message, status, body, headers);
        if (status == 404) return new NotFoundException(message, status, body, headers);
        if (status == 429) return new RateLimitException(message, status, body, headers);
        if (status >= 500) return new ServerException(message, status, body, headers);
        return new G2BulkException(message, status, body, headers);
    }
}
