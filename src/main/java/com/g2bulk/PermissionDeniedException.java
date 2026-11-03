package com.g2bulk;

import java.util.Map;

public class PermissionDeniedException extends G2BulkException {
    private static final long serialVersionUID = 1L;

    public PermissionDeniedException(String message, int status, Object body, Map<String, String> headers) {
        super(message, status, body, headers);
    }
}
