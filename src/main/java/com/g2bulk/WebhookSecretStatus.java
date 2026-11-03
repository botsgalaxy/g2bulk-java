package com.g2bulk;

import java.util.Map;

public final class WebhookSecretStatus {
    private final boolean hasSecret;
    private final String createdAt;

    WebhookSecretStatus(Map<String, Object> m) {
        this.hasSecret = Json.bool(m, "has_secret");
        this.createdAt = Json.str(m, "created_at");
    }

    public boolean hasSecret() {
        return hasSecret;
    }

    public String createdAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "WebhookSecretStatus{" + "hasSecret=" + hasSecret + ", " + "createdAt=" + createdAt + "}";
    }
}
