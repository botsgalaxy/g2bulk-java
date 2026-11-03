package com.g2bulk;

import java.util.Locale;
import java.util.Map;

public class BadRequestException extends G2BulkException {
    private static final long serialVersionUID = 1L;

    public static final String INSUFFICIENT_BALANCE = "insufficient_balance";
    public static final String OUT_OF_STOCK = "out_of_stock";
    public static final String INVALID_PLAYER = "invalid_player";
    public static final String INVALID_IDEMPOTENCY_KEY = "invalid_idempotency_key";

    public BadRequestException(String message, int status, Object body, Map<String, String> headers) {
        super(message, status, body, headers);
    }

    public String code() {
        return codeFor(getMessage());
    }

    static String codeFor(String message) {
        if (message == null) return null;
        String m = message.toLowerCase(Locale.ROOT);
        if (m.contains("enough balance")) return INSUFFICIENT_BALANCE;
        if (m.contains("enough stock") || m.contains("out of stock")) return OUT_OF_STOCK;
        if (m.contains("invalid player")) return INVALID_PLAYER;
        if (m.contains("idempotency key")) return INVALID_IDEMPOTENCY_KEY;
        return null;
    }
}
