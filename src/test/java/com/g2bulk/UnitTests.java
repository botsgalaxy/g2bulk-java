package com.g2bulk;

import static com.g2bulk.TestRunner.eq;
import static com.g2bulk.TestRunner.ok;
import static com.g2bulk.TestRunner.raises;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UnitTests {
    static final class FakeTransport implements Transport {
        final Deque<Object> responses = new ArrayDeque<>();
        final List<TransportRequest> calls = new ArrayList<>();

        FakeTransport respond(int status, String body) {
            return respond(status, body, Collections.singletonMap("Content-Type", "application/json"));
        }

        FakeTransport respond(int status, String body, Map<String, String> headers) {
            responses.add(new TransportResponse(status, headers, body));
            return this;
        }

        FakeTransport fail(IOException e) {
            responses.add(e);
            return this;
        }

        @Override
        public TransportResponse send(TransportRequest request) throws IOException {
            calls.add(request);
            Object next = responses.poll();
            if (next == null) throw new AssertionError("unexpected request " + request.method() + " " + request.uri());
            if (next instanceof IOException) throw (IOException) next;
            return (TransportResponse) next;
        }
    }

    static G2Bulk client(FakeTransport t, int maxRetries) {
        return G2Bulk.builder().apiKey("k").baseUrl("http://x").maxRetries(maxRetries).transport(t).build();
    }

    public static void main(String[] args) {
        TestRunner r = new TestRunner();

        r.test("retries GET on 503 then succeeds", () -> {
            FakeTransport t = new FakeTransport().respond(503, "{}").respond(200, "{\"success\":true,\"games\":[]}");
            eq(0, client(t, 2).games().list().size());
            eq(2, t.calls.size());
        });

        r.test("gives up after maxRetries and raises the matching error", () -> {
            FakeTransport t = new FakeTransport().respond(500, "{\"message\":\"boom\"}").respond(500, "{\"message\":\"boom\"}");
            raises(ServerException.class, () -> client(t, 1).account().me(), e -> e.getMessage().equals("boom") && e.status() == 500);
            eq(2, t.calls.size());
        });

        r.test("honours Retry-After and exposes it", () -> {
            Map<String, String> h = Collections.singletonMap("Retry-After", "0");
            FakeTransport t = new FakeTransport().respond(429, "{\"message\":\"slow down\"}", h).respond(429, "{\"message\":\"slow down\"}", h);
            raises(RateLimitException.class, () -> client(t, 1).account().me(),
                    e -> e.retryAfter().isPresent() && e.retryAfter().get().isZero() && e.getMessage().equals("slow down"));
            eq(2, t.calls.size());
        });

        r.test("orders always send an idempotency key and are retried with the same key", () -> {
            FakeTransport t = new FakeTransport().respond(502, "")
                    .respond(200, "{\"success\":true,\"order\":{\"order_id\":1,\"status\":\"PENDING\"}}");
            GameOrderSummary order = client(t, 2).games().order("pubgm",
                    GameOrderParams.builder().catalogueName("60 UC").playerId("1").build());
            eq(1L, order.orderId());
            eq("PENDING", order.status());
            String k0 = t.calls.get(0).header("X-Idempotency-Key");
            String k1 = t.calls.get(1).header("X-Idempotency-Key");
            eq(36, k0.length());
            eq(k0, k1);
            Map<String, Object> body = Json.obj(Json.parse(new String(t.calls.get(0).body(), StandardCharsets.UTF_8)));
            eq("60 UC", body.get("catalogue_name"));
            eq("1", body.get("player_id"));
            ok(!body.containsKey("server_id"), "null fields are omitted");
            ok(Arrays.equals(t.calls.get(0).body(), t.calls.get(1).body()), "same body on retry");
            eq("application/json", t.calls.get(0).header("Content-Type"));
        });

        r.test("unkeyed POSTs are not retried", () -> {
            FakeTransport t = new FakeTransport().respond(503, "{\"message\":\"down\"}");
            raises(ServerException.class, () -> client(t, 2).webhooks().rotateSecret());
            eq(1, t.calls.size());
        });

        r.test("network errors become ConnectionError", () -> {
            FakeTransport t = new FakeTransport().fail(new ConnectException("refused"));
            raises(ConnectionException.class, () -> client(t, 0).account().me(), e -> e.getCause() instanceof ConnectException);
        });

        r.test("network errors are retried for GET", () -> {
            FakeTransport t = new FakeTransport().fail(new IOException("reset")).respond(200, "{\"products\":[]}");
            eq(0, client(t, 1).catalog().products().size());
            eq(2, t.calls.size());
        });

        r.test("plain-text error bodies keep their message", () -> {
            FakeTransport t = new FakeTransport().respond(400, "invalid idempotency key: invalid length: 5 != 36",
                    Collections.singletonMap("Content-Type", "text/plain"));
            raises(BadRequestException.class,
                    () -> client(t, 2).purchases().create(1, PurchaseParams.builder().quantity(1).idempotencyKey("short").build()),
                    e -> "invalid_idempotency_key".equals(e.code()) && e.getMessage().equals("invalid idempotency key: invalid length: 5 != 36"));
        });

        r.test("error message falls back through error and detail.message", () -> {
            FakeTransport t = new FakeTransport()
                    .respond(404, "{\"error\":\"nope\"}")
                    .respond(403, "{\"detail\":{\"message\":\"denied\"}}")
                    .respond(400, "{\"success\":false,\"message\":\"You don't have enough balance to buy\"}");
            G2Bulk g2 = client(t, 0);
            raises(NotFoundException.class, () -> g2.purchases().get(1), e -> e.getMessage().equals("nope"));
            raises(PermissionDeniedException.class, () -> g2.purchases().get(1), e -> e.getMessage().equals("denied"));
            raises(BadRequestException.class, () -> g2.purchases().get(1), e -> "insufficient_balance".equals(e.code()));
        });

        r.test("public endpoints send no API key", () -> {
            FakeTransport t = new FakeTransport().respond(200, "{\"success\":true,\"products\":[]}");
            G2Bulk.builder().apiKey("secret").baseUrl("http://x").transport(t).build().catalog().products();
            eq(null, t.calls.get(0).header("X-API-Key"));
            ok(t.calls.get(0).header("User-Agent").startsWith("g2bulk-java/"), "user agent");
        });

        r.test("authenticated endpoints send the API key and query params", () -> {
            FakeTransport t = new FakeTransport().respond(200,
                    "{\"orders\":[{\"id\":5,\"user_id\":4600000000029447,\"total_price\":\"4.80\",\"is_refunded\":false,\"extra\":{\"x\":1}}],"
                            + "\"pagination\":{\"page\":1,\"limit\":2,\"total\":1,\"total_pages\":1},"
                            + "\"summary\":{\"orders\":1,\"completed\":1,\"in_progress\":0,\"failed\":0,\"cancelled\":0,\"spend\":4.8,\"refunded\":0}}");
            Page<Order> page = client(t, 0).purchases().list(OrderListParams.builder().limit(2).summary(true).productId(4).search("a b").build());
            eq("k", t.calls.get(0).header("X-API-Key"));
            eq("/v1/orders?limit=2&search=a%20b&summary=1&product_id=4", t.calls.get(0).uri().getRawPath() + "?" + t.calls.get(0).uri().getRawQuery());
            eq(4600000000029447L, page.items().get(0).userId());
            eq("4.80", page.items().get(0).totalPrice());
            eq(4.8, page.summary().get().spend());
            eq(2, page.pagination().limit());
        });

        r.test("delivery maps 202 and 410 to states", () -> {
            FakeTransport t = new FakeTransport()
                    .respond(202, "{\"order_id\":9,\"status\":\"PROCESSING\"}")
                    .respond(410, "{\"order_id\":9,\"status\":\"FAILED\",\"refunded\":true}");
            G2Bulk g2 = client(t, 0);
            eq(Delivery.State.PROCESSING, g2.purchases().delivery(9).state());
            Delivery failed = g2.purchases().delivery(9);
            eq(Delivery.State.FAILED, failed.state());
            ok(failed.refunded());
        });

        r.test("servers 403 is empty and checkPlayerId 400 is a result", () -> {
            FakeTransport t = new FakeTransport()
                    .respond(403, "{\"detail\":{\"message\":\"no servers\"}}")
                    .respond(400, "{\"name\":\"\",\"valid\":\"invalid\"}");
            G2Bulk g2 = client(t, 0);
            ok(!g2.games().servers("demogame").isPresent());
            PlayerCheck check = g2.games().validatePlayer(PlayerParams.builder().game("demogame").userId("1").build());
            ok(!check.valid());
        });

        r.test("missing API key fails before sending", () -> {
            FakeTransport t = new FakeTransport();
            G2Bulk g2 = G2Bulk.builder().apiKey("").baseUrl("http://x").transport(t).build();
            raises(G2BulkException.class, () -> g2.account().me(), e -> e.getMessage().contains("API key"));
            eq(0, t.calls.size());
        });

        r.test("json parses nested objects and arrays", () -> {
            Map<String, Object> m = Json.obj(Json.parse(" {\"a\": {\"b\": [1, 2, {\"c\": [true, false, null]}]}, \"e\": [] , \"f\": {}} "));
            List<?> b = (List<?>) Json.obj(m, "a").get("b");
            eq(1L, b.get(0));
            eq(Arrays.asList(true, false, null), Json.obj(b.get(2)).get("c"));
            eq(Collections.emptyList(), m.get("e"));
            eq(Collections.emptyMap(), m.get("f"));
        });

        r.test("json unicode escapes and string escapes", () -> {
            Object v = Json.parse("\"caf\\u00e9 \\ud83d\\ude00 \\\"q\\\" \\\\ \\/ \\n\\t\"");
            eq("caf\u00e9 \ud83d\ude00 \"q\" \\ / \n\t", v);
            eq(v, Json.parse(Json.write(v)));
            eq("\"\\u0001\"", Json.write("\u0001"));
        });

        r.test("json numbers: large longs, big integers, doubles", () -> {
            Map<String, Object> m = Json.obj(Json.parse("{\"id\":4600000000029447,\"max\":9223372036854775807,\"big\":92233720368547758070,\"d\":4.8,\"e\":-1.5e3,\"n\":-7}"));
            eq(4600000000029447L, m.get("id"));
            eq(Long.MAX_VALUE, m.get("max"));
            eq(new BigInteger("92233720368547758070"), m.get("big"));
            eq(4.8, m.get("d"));
            eq(-1500.0, m.get("e"));
            eq(-7L, m.get("n"));
            eq(4600000000029447L, Json.lng(m, "id"));
        });

        r.test("json round trip", () -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("s", "x\"y");
            m.put("n", null);
            m.put("l", 4600000000029447L);
            m.put("d", 1.25);
            m.put("i", 3.0);
            m.put("a", Arrays.asList(1L, "two", false, null));
            m.put("o", Collections.singletonMap("k", "\u00e9"));
            String text = Json.write(m);
            eq("{\"s\":\"x\\\"y\",\"n\":null,\"l\":4600000000029447,\"d\":1.25,\"i\":3,\"a\":[1,\"two\",false,null],\"o\":{\"k\":\"\u00e9\"}}", text);
            Map<String, Object> back = Json.obj(Json.parse(text));
            eq(m.get("a"), back.get("a"));
            eq(m.get("o"), back.get("o"));
            eq(3L, back.get("i"));
            ok(back.containsKey("n") && back.get("n") == null);
        });

        r.test("json rejects malformed input", () -> {
            for (String bad : new String[] {"", "{", "[1,]", "{\"a\" 1}", "tru", "\"abc", "1 2", "{\"a\":1,}"}) {
                raises(IllegalArgumentException.class, () -> Json.parse(bad));
            }
        });

        r.test("webhook verify round trip with injected clock", () -> {
            String secret = "whsec_MfKQ9r8GKYqrTwjUPD8ILPZIo2LaLaSw";
            byte[] body = "{\"event\":\"ping\",\"event_id\":\"evt_1\"}".getBytes(StandardCharsets.UTF_8);
            String sig = java.util.Base64.getEncoder().encodeToString(Webhooks.sign(secret, "msg_1", "1000", body));
            Map<String, String> h = new LinkedHashMap<>();
            h.put("Webhook-Id", "msg_1");
            h.put("Webhook-Timestamp", "1000");
            h.put("Webhook-Signature", "v1," + sig);
            WebhookEvent e = Webhooks.verify(secret, h, body, Duration.ofSeconds(10), java.time.Instant.ofEpochSecond(1005));
            ok(e.isPing());
            eq("evt_1", e.eventId());
            raises(WebhookVerificationException.class,
                    () -> Webhooks.verify(secret, h, body, Duration.ofSeconds(1), java.time.Instant.ofEpochSecond(1005)));
        });

        System.exit(r.run() == 0 ? 0 : 1);
    }
}
