package com.g2bulk;

public class ConnectionException extends G2BulkException {
    private static final long serialVersionUID = 1L;

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
