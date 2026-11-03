package com.g2bulk;

import java.util.Map;

public class NotFoundException extends G2BulkException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message, int status, Object body, Map<String, String> headers) {
        super(message, status, body, headers);
    }
}
