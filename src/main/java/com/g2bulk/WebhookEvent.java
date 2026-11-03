package com.g2bulk;

import java.util.Collections;
import java.util.Map;

public final class WebhookEvent {
    private final String raw;
    private final Map<String, Object> data;

    WebhookEvent(String raw, Map<String, Object> data) {
        this.raw = raw;
        this.data = Collections.unmodifiableMap(data);
    }

    public String raw() {
        return raw;
    }

    public Map<String, Object> data() {
        return data;
    }

    public Object get(String key) {
        return data.get(key);
    }

    public String eventId() {
        return Json.str(data, "event_id");
    }

    public String event() {
        return Json.str(data, "event");
    }

    public boolean isTopUp() {
        String e = event();
        return e != null && e.startsWith("topup.");
    }

    public boolean isPing() {
        return "ping".equals(event());
    }

    public boolean isOrder() {
        String e = event();
        return e != null && !isTopUp() && !isPing() && data.containsKey("product_id");
    }

    public Long orderId() {
        return Json.lngOrNull(data, "order_id");
    }

    public Long productId() {
        return Json.lngOrNull(data, "product_id");
    }

    public String status() {
        return Json.str(data, "status");
    }

    public boolean isRefunded() {
        return Json.bool(data, "is_refunded");
    }

    public String deliveredAt() {
        return Json.str(data, "delivered_at");
    }

    public String gameCode() {
        return Json.str(data, "game_code");
    }

    public String gameName() {
        return Json.str(data, "game_name");
    }

    public String playerId() {
        return Json.str(data, "player_id");
    }

    public String playerName() {
        return Json.str(data, "player_name");
    }

    public String serverId() {
        return Json.str(data, "server_id");
    }

    public String denomId() {
        return Json.str(data, "denom_id");
    }

    public Double price() {
        return Json.dblOrNull(data, "price");
    }

    public String message() {
        return Json.str(data, "message");
    }

    public String remark() {
        return Json.str(data, "remark");
    }

    public String timestamp() {
        return Json.str(data, "timestamp");
    }

    @Override
    public String toString() {
        return "WebhookEvent" + raw;
    }
}
