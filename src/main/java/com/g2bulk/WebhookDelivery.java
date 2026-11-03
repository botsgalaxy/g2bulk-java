package com.g2bulk;

import java.util.Map;

public final class WebhookDelivery {
    private final long id;
    private final String eventId;
    private final String eventType;
    private final String kind;
    private final long orderId;
    private final String url;
    private final String payload;
    private final String status;
    private final int attempts;
    private final String nextAttemptAt;
    private final String lastAttemptAt;
    private final int lastStatusCode;
    private final String lastError;
    private final String deliveredAt;
    private final String createdAt;

    WebhookDelivery(Map<String, Object> m) {
        this.id = Json.lng(m, "id");
        this.eventId = Json.str(m, "event_id");
        this.eventType = Json.str(m, "event_type");
        this.kind = Json.str(m, "kind");
        this.orderId = Json.lng(m, "order_id");
        this.url = Json.str(m, "url");
        this.payload = Json.str(m, "payload");
        this.status = Json.str(m, "status");
        this.attempts = Json.integer(m, "attempts");
        this.nextAttemptAt = Json.str(m, "next_attempt_at");
        this.lastAttemptAt = Json.str(m, "last_attempt_at");
        this.lastStatusCode = Json.integer(m, "last_status_code");
        this.lastError = Json.str(m, "last_error");
        this.deliveredAt = Json.str(m, "delivered_at");
        this.createdAt = Json.str(m, "created_at");
    }

    public long id() {
        return id;
    }

    public String eventId() {
        return eventId;
    }

    public String eventType() {
        return eventType;
    }

    public String kind() {
        return kind;
    }

    public long orderId() {
        return orderId;
    }

    public String url() {
        return url;
    }

    public String payload() {
        return payload;
    }

    public String status() {
        return status;
    }

    public int attempts() {
        return attempts;
    }

    public String nextAttemptAt() {
        return nextAttemptAt;
    }

    public String lastAttemptAt() {
        return lastAttemptAt;
    }

    public int lastStatusCode() {
        return lastStatusCode;
    }

    public String lastError() {
        return lastError;
    }

    public String deliveredAt() {
        return deliveredAt;
    }

    public String createdAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "WebhookDelivery{" + "id=" + id + ", " + "eventId=" + eventId + ", " + "eventType=" + eventType + ", " + "kind=" + kind + ", " + "orderId=" + orderId + ", " + "url=" + url + ", " + "payload=" + payload + ", " + "status=" + status + ", " + "attempts=" + attempts + ", " + "nextAttemptAt=" + nextAttemptAt + ", " + "lastAttemptAt=" + lastAttemptAt + ", " + "lastStatusCode=" + lastStatusCode + ", " + "lastError=" + lastError + ", " + "deliveredAt=" + deliveredAt + ", " + "createdAt=" + createdAt + "}";
    }
}
