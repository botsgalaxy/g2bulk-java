package com.g2bulk;

import java.util.Map;

public class ServerException extends G2BulkException {
    private static final long serialVersionUID = 1L;

    public ServerException(String message, int status, Object body, Map<String, String> headers) {
        super(message, status, body, headers);
    }
}
