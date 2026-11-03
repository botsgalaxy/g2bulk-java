package com.g2bulk;

public class WaitTimeoutException extends G2BulkException {
    private static final long serialVersionUID = 1L;

    public WaitTimeoutException(String message) {
        super(message);
    }
}
