package com.g2bulk;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

final class Http {
    static final String VERSION = "0.1.0";
    private static final Set<Integer> RETRY_STATUSES = new HashSet<>(Arrays.asList(429, 500, 502, 503, 504));

    final String baseUrl;
    private final String apiKey;
    private final Duration timeout;
    private final int maxRetries;
    private final Transport transport;

    Http(String apiKey, String baseUrl, Duration timeout, int maxRetries, Transport transport) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        this.transport = transport;
    }

    static final class Request {
        final String method;
        final String path;
        final Map<String, Object> query = new LinkedHashMap<>();
        Map<String, Object> body;
        boolean auth = true;
        String idempotencyKey;
        Boolean retry;
        final Set<Integer> expect = new HashSet<>();
        boolean raw;

        private Request(String method, String path) {
            this.method = method;
            this.path = path;
        }

        static Request get(String path) {
            return new Request("GET", path);
        }

        static Request post(String path) {
            return new Request("POST", path);
        }

        Request query(String key, Object value) {
            query.put(key, value);
            return this;
        }

        Request body(Map<String, Object> body) {
            this.body = body;
            return this;
        }

        Request publicEndpoint() {
            this.auth = false;
            return this;
        }

        Request idempotencyKey(String key) {
            this.idempotencyKey = key;
            return this;
        }

        Request retry() {
            this.retry = Boolean.TRUE;
            return this;
        }

        Request expect(Integer... statuses) {
            expect.addAll(Arrays.asList(statuses));
            return this;
        }

        Request raw() {
            this.raw = true;
            return this;
        }
    }

    static final class Response {
        final int status;
        final Object data;
        final String text;
        final Map<String, String> headers;

        Response(int status, Object data, String text, Map<String, String> headers) {
            this.status = status;
            this.data = data;
            this.text = text;
            this.headers = headers;
        }

        Map<String, Object> obj() {
            return Json.obj(data);
        }
    }

    static Map<String, Object> body(Object... pairs) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int k = 0; k < pairs.length; k += 2) {
            if (pairs[k + 1] != null) map.put((String) pairs[k], pairs[k + 1]);
        }
        return map;
    }

    static String newIdempotencyKey() {
        return UUID.randomUUID().toString();
    }

    Response send(Request req) {
        URI uri = URI.create(baseUrl + req.path + queryString(req.query));
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", req.raw ? "text/csv" : "application/json");
        headers.put("User-Agent", "g2bulk-java/" + VERSION);
        if (req.auth) {
            if (apiKey == null || apiKey.isEmpty()) {
                throw new G2BulkException("An API key is required. Pass apiKey or set G2BULK_API_KEY.");
            }
            headers.put("X-API-Key", apiKey);
        }
        byte[] payload = null;
        if (req.body != null) {
            headers.put("Content-Type", "application/json");
            payload = Json.write(req.body).getBytes(StandardCharsets.UTF_8);
        }
        if (req.idempotencyKey != null) headers.put("X-Idempotency-Key", req.idempotencyKey);
        boolean retryable = req.retry != null ? req.retry : ("GET".equals(req.method) || req.idempotencyKey != null);
        TransportRequest treq = new TransportRequest(req.method, uri, headers, payload, timeout);

        for (int attempt = 0; ; attempt++) {
            TransportResponse res;
            try {
                res = transport.send(treq);
            } catch (IOException e) {
                if (retryable && attempt < maxRetries) {
                    sleep(backoff(attempt));
                    continue;
                }
                String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                throw new ConnectionException("Could not reach " + baseUrl + ": " + reason, e);
            }
            String text = new String(res.body(), StandardCharsets.UTF_8);
            Object data = req.raw ? text : parse(text);
            int status = res.status();
            if ((status >= 200 && status < 300) || req.expect.contains(status)) {
                return new Response(status, data, text, res.headers());
            }
            if (retryable && RETRY_STATUSES.contains(status) && attempt < maxRetries) {
                sleep(retryDelay(res.headers().get("retry-after"), attempt));
                continue;
            }
            throw G2BulkException.forStatus(status, messageOf(data, text, status), data, res.headers());
        }
    }

    private static String queryString(Map<String, Object> query) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : query.entrySet()) {
            Object v = e.getValue();
            if (v == null) continue;
            String s = v.toString();
            if (s.isEmpty()) continue;
            sb.append(sb.length() == 0 ? '?' : '&').append(encode(e.getKey())).append('=').append(encode(s));
        }
        return sb.toString();
    }

    static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static Object parse(String text) {
        if (text.isEmpty()) return null;
        try {
            return Json.parse(text);
        } catch (IllegalArgumentException e) {
            return text;
        }
    }

    static String messageOf(Object data, String text, int status) {
        if (data instanceof Map) {
            Map<String, Object> d = Json.obj(data);
            Object message = d.get("message");
            if (message instanceof String && !((String) message).isEmpty()) return (String) message;
            Object error = d.get("error");
            if (error instanceof String && !((String) error).isEmpty()) return (String) error;
            Object detail = Json.obj(d, "detail").get("message");
            if (detail instanceof String) return (String) detail;
        }
        if (text != null && !text.isEmpty() && text.length() < 500) return text.trim();
        return "Request failed with status " + status;
    }

    private static long backoff(int attempt) {
        double base = Math.min(500.0 * Math.pow(2, attempt), 8000.0);
        return (long) (base * (0.75 + ThreadLocalRandom.current().nextDouble() * 0.5));
    }

    private static long retryDelay(String retryAfter, int attempt) {
        if (retryAfter != null) {
            try {
                double seconds = Double.parseDouble(retryAfter.trim());
                if (seconds >= 0 && !Double.isInfinite(seconds)) return (long) (Math.min(seconds, 60) * 1000);
            } catch (NumberFormatException ignored) {
            }
        }
        return backoff(attempt);
    }

    static void sleep(long millis) {
        if (millis <= 0) return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new G2BulkException("Interrupted while waiting", e);
        }
    }
}
