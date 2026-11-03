package com.g2bulk;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class Webhooks {
    public static final Duration DEFAULT_TOLERANCE = Duration.ofMinutes(5);

    private final Http http;

    Webhooks(Http http) {
        this.http = http;
    }

    public Page<WebhookDelivery> deliveries() {
        return deliveries(DeliveryListParams.defaults());
    }

    public Page<WebhookDelivery> deliveries(DeliveryListParams params) {
        Http.Request req = Http.Request.get("/v1/webhooks/deliveries")
                .query("page", params.page()).query("limit", params.limit())
                .query("status", params.status()).query("order_id", params.orderId());
        Map<String, Object> data = http.send(req).obj();
        List<WebhookDelivery> items = new ArrayList<>();
        for (Map<String, Object> o : Json.objs(data, "deliveries")) items.add(new WebhookDelivery(o));
        long limit = Json.lng(data, "limit");
        if (limit == 0) limit = params.limit() != null ? params.limit() : 20;
        long total = Json.lng(data, "total");
        Map<String, Object> pagination = Http.body(
                "page", Json.lng(data, "page"),
                "limit", limit,
                "total", total,
                "total_pages", (total + limit - 1) / limit);
        return new Page<>(items, new Pagination(pagination), null);
    }

    public void resend(long deliveryId) {
        http.send(Http.Request.post("/v1/webhooks/deliveries/" + deliveryId + "/resend"));
    }

    public String sendTest(String url) {
        Map<String, Object> data = http.send(Http.Request.post("/v1/webhooks/test").body(Http.body("url", url))).obj();
        return Json.str(data, "event_id");
    }

    public WebhookSecretStatus secret() {
        return new WebhookSecretStatus(http.send(Http.Request.get("/v1/webhooks/secret")).obj());
    }

    public String rotateSecret() {
        return Json.str(http.send(Http.Request.post("/v1/webhooks/secret")).obj(), "secret");
    }

    public static WebhookEvent verify(String secret, Map<String, String> headers, byte[] body) {
        return verify(secret, headers, body, DEFAULT_TOLERANCE, Instant.now());
    }

    public static WebhookEvent verify(String secret, Map<String, String> headers, String body) {
        return verify(secret, headers, body.getBytes(StandardCharsets.UTF_8));
    }

    public static WebhookEvent verify(String secret, Map<String, String> headers, byte[] body, Duration tolerance, Instant now) {
        String id = header(headers, "webhook-id");
        String timestamp = header(headers, "webhook-timestamp");
        String signature = header(headers, "webhook-signature");
        if (isBlank(id) || isBlank(timestamp) || isBlank(signature)) {
            throw new WebhookVerificationException("Missing webhook-id, webhook-timestamp or webhook-signature header");
        }
        long ts;
        try {
            ts = Long.parseLong(timestamp.trim());
        } catch (NumberFormatException e) {
            throw new WebhookVerificationException("Invalid webhook-timestamp header");
        }
        if (Math.abs(now.getEpochSecond() - ts) > tolerance.getSeconds()) {
            throw new WebhookVerificationException("Webhook timestamp is outside the allowed tolerance");
        }
        byte[] expected = sign(secret, id, timestamp, body);
        boolean ok = false;
        for (String part : signature.split(" ")) {
            int comma = part.indexOf(',');
            if (comma < 0 || !"v1".equals(part.substring(0, comma))) continue;
            byte[] given;
            try {
                given = Base64.getDecoder().decode(part.substring(comma + 1));
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (MessageDigest.isEqual(given, expected)) ok = true;
        }
        if (!ok) throw new WebhookVerificationException("Webhook signature does not match");
        String raw = new String(body, StandardCharsets.UTF_8);
        Object parsed;
        try {
            parsed = Json.parse(raw);
        } catch (IllegalArgumentException e) {
            throw new WebhookVerificationException("Webhook body is not valid JSON");
        }
        if (!(parsed instanceof Map)) throw new WebhookVerificationException("Webhook body is not a JSON object");
        return new WebhookEvent(raw, Json.obj(parsed));
    }

    static byte[] sign(String secret, String id, String timestamp, byte[] body) {
        String encoded = secret.startsWith("whsec_") ? secret.substring(6) : secret;
        byte[] key;
        try {
            key = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            throw new WebhookVerificationException("Webhook secret is not valid base64");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            mac.update((id + "." + timestamp + ".").getBytes(StandardCharsets.UTF_8));
            return mac.doFinal(body);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new WebhookVerificationException("Could not compute webhook signature: " + e.getMessage());
        }
    }

    private static String header(Map<String, String> headers, String name) {
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) return e.getValue();
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
