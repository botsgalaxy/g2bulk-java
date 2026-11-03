package com.g2bulk;

import static com.g2bulk.TestRunner.eq;
import static com.g2bulk.TestRunner.ok;
import static com.g2bulk.TestRunner.raises;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class E2ETests {
    static Path e2eDir;

    static Map<String, String> loadEnv(String name) throws IOException {
        Map<String, String> env = new HashMap<>();
        for (String line : Files.readAllLines(e2eDir.resolve(".env." + name), StandardCharsets.UTF_8)) {
            int i = line.indexOf('=');
            if (i > 0) env.put(line.substring(0, i), line.substring(i + 1).trim());
        }
        return env;
    }

    static void finishGameOrder(long id, String status) {
        try {
            Process p = new ProcessBuilder(e2eDir.resolve("finish-game-order.sh").toString(), Long.toString(id), status)
                    .redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!p.waitFor(30, TimeUnit.SECONDS) || p.exitValue() != 0) {
                throw new IllegalStateException("finish-game-order.sh failed: " + out);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        e2eDir = Paths.get(System.getProperty("g2bulk.e2e.dir", "../e2e")).toAbsolutePath().normalize();
        Map<String, String> env = loadEnv("java");
        Map<String, Object> vector = Json.obj(Json.parse(new String(Files.readAllBytes(Paths.get("testdata", "webhook-vector.json")), StandardCharsets.UTF_8)));
        String baseUrl = env.get("G2BULK_BASE_URL");
        G2Bulk g2 = G2Bulk.builder().apiKey(env.get("G2BULK_API_KEY")).baseUrl(baseUrl).build();
        String yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1).toString();
        String tomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1).toString();
        TestRunner r = new TestRunner();

        r.test("account", () -> {
            User me = g2.account().me();
            eq(env.get("G2BULK_USER_ID"), Long.toString(me.userId()));
            ok(me.balance() >= 0);
            G2Bulk bad = G2Bulk.builder().apiKey("invalid").baseUrl(baseUrl).build();
            raises(AuthenticationException.class, () -> bad.account().me());
        });

        r.test("catalog", () -> {
            List<Category> categories = g2.catalog().categories();
            ok(!categories.isEmpty());
            Category withProducts = categories.stream().filter(c -> c.productCount() > 0).findFirst().orElseThrow();
            ok(!g2.catalog().categoryProducts(withProducts.id()).isEmpty());
            ok(g2.catalog().products().stream().anyMatch(p -> p.id() == 4));
            Product product = g2.catalog().product(4);
            eq(4L, product.id());
            ok(product.unitPrice() > 0);
            raises(NotFoundException.class, () -> g2.catalog().product(999999));
        });

        r.test("inventory purchase, delivery and idempotency", () -> {
            double price = g2.catalog().product(4).unitPrice();
            double before = g2.account().me().balance();
            String key = UUID.randomUUID().toString();
            PurchaseParams params = PurchaseParams.builder().quantity(1).description("sdk e2e").idempotencyKey(key).build();
            PurchaseResult first = g2.purchases().create(4, params);
            PurchaseResult again = g2.purchases().create(4, params);
            eq("COMPLETED", first.status());
            eq(1, first.deliveryItems().size());
            eq(first.orderId(), again.orderId());
            double after = g2.account().me().balance();
            ok(Math.abs(before - after - price) < 0.001, "charged once: " + before + " -> " + after);
            Delivery delivery = g2.purchases().delivery(first.orderId());
            eq(Delivery.State.READY, delivery.state());
            eq(first.deliveryItems(), delivery.deliveryItems());
            Order order = g2.purchases().get(first.orderId());
            eq("COMPLETED", order.status());
            eq(4L, order.productId());
        });

        r.test("provider purchase waits for delivery", () -> {
            PurchaseResult result = g2.purchases().create(1, PurchaseParams.builder().quantity(1).build());
            ok(result.status().equals("PENDING") || result.status().equals("COMPLETED"), result.status());
            Delivery delivery = g2.purchases().waitForDelivery(result.orderId(), Duration.ofSeconds(1), Duration.ofSeconds(30));
            eq(Delivery.State.READY, delivery.state());
            eq(1, delivery.deliveryItems().size());
        });

        r.test("purchase errors", () -> {
            raises(BadRequestException.class, () -> g2.purchases().create(7, PurchaseParams.builder().quantity(1).build()),
                    e -> "out_of_stock".equals(e.code()));
            raises(BadRequestException.class, () -> g2.purchases().create(3, PurchaseParams.builder().quantity(50).build()),
                    e -> "insufficient_balance".equals(e.code()));
            raises(BadRequestException.class, () -> g2.purchases().create(4, PurchaseParams.builder().quantity(1).idempotencyKey("short").build()),
                    e -> "invalid_idempotency_key".equals(e.code()));
            raises(NotFoundException.class, () -> g2.purchases().get(999999999));
        });

        r.test("order list, export and transactions", () -> {
            Page<Order> page = g2.purchases().list(OrderListParams.builder().limit(2).summary(true).build());
            ok(!page.items().isEmpty() && page.items().size() <= 2);
            ok(page.summary().get().orders() >= 2);
            eq(2, page.pagination().limit());
            Page<Order> filtered = g2.purchases().list(OrderListParams.builder().productId(4).status("completed").build());
            ok(filtered.items().stream().allMatch(o -> o.productId() == 4));
            String csv = g2.purchases().exportCsv(ExportParams.builder().from(yesterday).to(tomorrow).build());
            ok(csv.startsWith("order_id,"), csv);
            ok(csv.contains(Long.toString(page.items().get(0).id())));
            Page<Transaction> txns = g2.transactions().list(ListParams.builder().limit(5).build());
            ok(txns.items().stream().anyMatch(t -> "charge_balance".equals(t.transactionType())));
            raises(BadRequestException.class, () -> g2.purchases().exportCsv(ExportParams.builder().from("").to("").build()));
        });

        r.test("game lookups", () -> {
            ok(g2.games().list().stream().anyMatch(g -> "demogame".equals(g.code())));
            ok(g2.games().fields("demogame").fields().contains("userid"));
            ok(!g2.games().servers("demogame").isPresent());
            PlayerCheck check = g2.games().validatePlayer(PlayerParams.builder().game("demogame").userId("123456").build());
            ok(!check.valid());
            GameCatalogue catalogue = g2.games().catalogue("demogame");
            eq("demogame", catalogue.game().code());
            ok(catalogue.catalogues().stream().anyMatch(c -> "100 Gems".equals(c.name())));
            Eta eta = g2.games().eta("demogame", "100 Gems");
            ok(eta.label() != null && !eta.label().isEmpty());
            raises(NotFoundException.class, () -> g2.games().catalogue("no_such_game"));
        });

        r.test("game order lifecycle", () -> {
            String key = UUID.randomUUID().toString();
            GameOrderParams params = GameOrderParams.builder().catalogueName("100 Gems").playerId("123456").serverId("2001")
                    .remark("sdk-e2e").idempotencyKey(key).build();
            GameOrderSummary order = g2.games().order("demogame", params);
            GameOrderSummary again = g2.games().order("demogame", params);
            eq("PENDING", order.status());
            eq(order.orderId(), again.orderId());
            GameOrderSummary status = g2.games().status(order.orderId(), "demogame");
            eq(order.orderId(), status.orderId());
            eq("sdk-e2e", status.remark());
            eq("demogame", g2.games().getOrder(order.orderId()).gameCode());
            Page<GameOrder> listed = g2.games().listOrders(OrderListParams.builder().game("demogame").limit(5).summary(true).build());
            ok(listed.items().stream().anyMatch(o -> o.orderId() == order.orderId()));
            CompletableFuture<Void> finisher = CompletableFuture.runAsync(() -> {
                Http.sleep(1500);
                finishGameOrder(order.orderId(), "COMPLETED");
            });
            GameOrder done = g2.games().waitForOrder(order.orderId(), Duration.ofMillis(500), Duration.ofSeconds(30));
            finisher.get(30, TimeUnit.SECONDS);
            eq("COMPLETED", done.status());
            String csv = g2.games().exportCsv(ExportParams.builder().from(yesterday).to(tomorrow).game("demogame").build());
            ok(csv.contains(Long.toString(order.orderId())));
        });

        r.test("game order errors and wait timeout", () -> {
            raises(NotFoundException.class, () -> g2.games().order("demogame",
                    GameOrderParams.builder().catalogueName("No Such Pack").playerId("123456").serverId("2001").build()));
            raises(NotFoundException.class, () -> g2.games().status(999999999, "demogame"));
            GameOrderSummary order = g2.games().order("demogame",
                    GameOrderParams.builder().catalogueName("100 Gems").playerId("123456").serverId("2001").build());
            raises(WaitTimeoutException.class, () -> g2.games().waitForOrder(order.orderId(), Duration.ofMillis(500), Duration.ofMillis(1500)));
            finishGameOrder(order.orderId(), "FAILED");
            GameOrder failed = g2.games().waitForOrder(order.orderId(), Duration.ofMillis(500), Duration.ofSeconds(5));
            eq("FAILED", failed.status());
        });

        r.test("webhook management", () -> {
            String secret = g2.webhooks().rotateSecret();
            ok(secret.startsWith("whsec_"));
            ok(g2.webhooks().secret().hasSecret());
            String eventId = g2.webhooks().sendTest("https://example.com/g2bulk-sdk-test");
            ok(eventId.startsWith("evt_test_"), eventId);
            Page<WebhookDelivery> deliveries = g2.webhooks().deliveries(DeliveryListParams.builder().limit(10).build());
            WebhookDelivery delivery = deliveries.items().stream().filter(d -> eventId.equals(d.eventId())).findFirst()
                    .orElseThrow(() -> new AssertionError("test delivery is listed"));
            eq(10, deliveries.pagination().limit());
            g2.webhooks().resend(delivery.id());
            raises(NotFoundException.class, () -> g2.webhooks().resend(999999999));
            raises(BadRequestException.class, () -> g2.webhooks().sendTest("http://localhost/hook"));
        });

        r.test("webhook verification", () -> {
            String secret = (String) vector.get("secret");
            String body = (String) vector.get("body");
            String signature = (String) vector.get("signature");
            Instant now = Instant.ofEpochSecond(Long.parseLong((String) vector.get("timestamp")));
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("webhook-id", (String) vector.get("id"));
            headers.put("webhook-timestamp", (String) vector.get("timestamp"));
            headers.put("webhook-signature", signature);
            WebhookEvent event = Webhooks.verify(secret, headers, body.getBytes(StandardCharsets.UTF_8), Webhooks.DEFAULT_TOLERANCE, now);
            eq(2432232314L, event.get("test"));
            eq(body, event.raw());
            Map<String, String> multi = new LinkedHashMap<>(headers);
            multi.put("webhook-signature", "v1,AAAA " + signature);
            ok(Webhooks.verify(secret, multi, body.getBytes(StandardCharsets.UTF_8), Webhooks.DEFAULT_TOLERANCE, now) != null);
            Map<String, String> mixedCase = new LinkedHashMap<>();
            mixedCase.put("Webhook-Id", headers.get("webhook-id"));
            mixedCase.put("WEBHOOK-TIMESTAMP", headers.get("webhook-timestamp"));
            mixedCase.put("Webhook-Signature", signature);
            ok(Webhooks.verify(secret, mixedCase, body.getBytes(StandardCharsets.UTF_8), Webhooks.DEFAULT_TOLERANCE, now) != null);
            raises(WebhookVerificationException.class,
                    () -> Webhooks.verify(secret, headers, (body + " ").getBytes(StandardCharsets.UTF_8), Webhooks.DEFAULT_TOLERANCE, now));
            raises(WebhookVerificationException.class, () -> Webhooks.verify(secret, headers, body));
            Map<String, String> missing = new LinkedHashMap<>();
            missing.put("webhook-id", headers.get("webhook-id"));
            raises(WebhookVerificationException.class, () -> Webhooks.verify(secret, missing, body));
        });

        System.exit(r.run() == 0 ? 0 : 1);
    }
}
