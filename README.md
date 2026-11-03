# G2Bulk for Java

The official client for the [G2Bulk reseller API](https://api.g2bulk.com/docs): game top-ups, gift cards and digital vouchers. Java 11 or newer, no runtime dependencies (built on `java.net.http.HttpClient`).

Maven:

```xml
<dependency>
  <groupId>com.g2bulk</groupId>
  <artifactId>g2bulk-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("com.g2bulk:g2bulk-java:0.1.0")
```

## Quickstart

```java
import com.g2bulk.*;

G2Bulk g2 = G2Bulk.builder().apiKey(System.getenv("G2BULK_API_KEY")).build();

User me = g2.account().me();
System.out.println("Balance: $" + me.balance());

GameCatalogue catalogue = g2.games().catalogue("pubgm");
GameOrderSummary order = g2.games().order("pubgm", GameOrderParams.builder()
        .catalogueName(catalogue.catalogues().get(0).name())
        .playerId("5679523421")
        .remark("order-8812")
        .build());
GameOrder done = g2.games().waitForOrder(order.orderId());
System.out.println(done.status());
```

`G2Bulk.fromEnv()` reads `G2BULK_API_KEY` and `G2BULK_BASE_URL`; the builder falls back to the same variables for anything you do not set. Keep your key on the server: it spends your balance. A client is thread-safe; build one and share it.

## Options

```java
G2Bulk g2 = G2Bulk.builder()
        .apiKey("…")
        .baseUrl("https://api.g2bulk.com")
        .timeout(Duration.ofSeconds(30))
        .maxRetries(2)
        .transport(customTransport)
        .build();
```

`transport` takes any `com.g2bulk.Transport` (one method, `TransportResponse send(TransportRequest)`), which is handy for tests or for routing through your own HTTP stack.

## Account

```java
User me = g2.account().me();
long id = me.userId();
double balance = me.balance();
```

## Gift cards and vouchers

```java
List<Category> categories = g2.catalog().categories();
List<Product> products = g2.catalog().categoryProducts(categories.get(0).id());
List<Product> everything = g2.catalog().products();
Product product = g2.catalog().product(products.get(0).id());

PurchaseResult purchase = g2.purchases().create(product.id(),
        PurchaseParams.builder().quantity(2).description("order-8812").build());
if (purchase.status().equals("COMPLETED")) {
    System.out.println(purchase.deliveryItems());
} else {
    Delivery delivery = g2.purchases().waitForDelivery(purchase.orderId());
    if (delivery.state() == Delivery.State.READY) System.out.println(delivery.deliveryItems());
    else System.out.println("Failed and refunded: " + delivery.message());
}

Page<Order> page = g2.purchases().list(OrderListParams.builder().status("completed").limit(50).summary(true).build());
page.summary().ifPresent(s -> System.out.println(s.spend()));
Order order = g2.purchases().get(page.items().get(0).id());
Delivery delivery = g2.purchases().delivery(order.id());
String csv = g2.purchases().exportCsv(ExportParams.builder().from("2026-10-01").to("2026-10-31").tz("Asia/Dhaka").build());
```

`delivery()` never throws for a pending or failed order: check `state()`, which is `READY` (HTTP 200), `PROCESSING` (202) or `FAILED` (410). `waitForDelivery(id, interval, timeout)` defaults to polling every 3 s for up to 2 minutes.

`OrderListParams` takes `page`, `limit`, `search`, `status` (`completed`, `in_progress`, `failed`, `cancelled`), `from`, `to`, `tz`, `source`, `summary`, plus `productId` for product orders and `game` for game orders.

## Game top-ups

```java
List<Game> games = g2.games().list();
GameFields fields = g2.games().fields("mlbb");
Optional<Map<String, String>> servers = g2.games().servers("genshin");
PlayerCheck player = g2.games().validatePlayer(PlayerParams.builder()
        .game("mlbb").userId("123456789").serverId("2001").build());
if (!player.valid()) throw new IllegalArgumentException("Unknown player");

GameCatalogue catalogue = g2.games().catalogue("mlbb");
Eta eta = g2.games().eta("mlbb", catalogue.catalogues().get(0).name());

GameOrderSummary order = g2.games().order("mlbb", GameOrderParams.builder()
        .catalogueName(catalogue.catalogues().get(0).name())
        .playerId("123456789")
        .serverId("2001")
        .callbackUrl("https://your-domain.com/webhooks/g2bulk")
        .build());

GameOrderSummary status = g2.games().status(order.orderId(), "mlbb");
GameOrder full = g2.games().getOrder(order.orderId());
GameOrder finished = g2.games().waitForOrder(order.orderId(), Duration.ofSeconds(5), Duration.ofMinutes(10));
Page<GameOrder> history = g2.games().listOrders(OrderListParams.builder().game("mlbb").status("failed").build());
String csv = g2.games().exportCsv(ExportParams.builder().from("2026-10-01").to("2026-10-31").game("mlbb").build());
```

`servers()` returns `Optional.empty()` for games that do not use servers. `validatePlayer()` returns a result with `valid() == false` for an unknown player instead of throwing. `waitForOrder()` returns the final order, `COMPLETED` or `FAILED` (failed orders are refunded to your balance), and throws `WaitTimeoutException` if it is still pending when the timeout runs out.

## Transactions

```java
Page<Transaction> page = g2.transactions().list(ListParams.builder().page(1).limit(50).build());
for (Transaction t : page.items()) System.out.println(t.transactionType() + " " + t.amount());
```

## Webhooks

```java
String secret = g2.webhooks().rotateSecret();
WebhookSecretStatus status = g2.webhooks().secret();
String eventId = g2.webhooks().sendTest("https://your-domain.com/webhooks/g2bulk");
Page<WebhookDelivery> deliveries = g2.webhooks().deliveries(DeliveryListParams.builder().status("failed").build());
g2.webhooks().resend(deliveries.items().get(0).id());
```

Verify every webhook against the raw request body with `Webhooks.verify(secret, headers, body)`. Header names are matched case-insensitively. An overload takes a `Duration` tolerance (default 5 minutes) and the current `Instant`. Example with the JDK's built-in server:

```java
server.createContext("/webhooks/g2bulk", exchange -> {
    byte[] body = exchange.getRequestBody().readAllBytes();
    Map<String, String> headers = new HashMap<>();
    exchange.getRequestHeaders().forEach((k, v) -> headers.put(k, v.get(0)));
    int code;
    try {
        WebhookEvent event = Webhooks.verify(System.getenv("G2BULK_WEBHOOK_SECRET"), headers, body);
        if ("topup.completed".equals(event.event())) markDelivered(event.orderId());
        code = 200;
    } catch (WebhookVerificationException e) {
        code = 400;
    }
    exchange.sendResponseHeaders(code, -1);
    exchange.close();
});
```

With Spring, take the body as `@RequestBody byte[] body` and the headers as `@RequestHeader Map<String, String> headers`.

`WebhookEvent` exposes the top-up, order and ping fields (`event()`, `eventId()`, `orderId()`, `status()`, `gameCode()`, `playerId()`, `productId()`, `isRefunded()`, …), `isTopUp()`, `isOrder()`, `isPing()`, the parsed `data()` map and the `raw()` JSON. Deliveries are retried for about three days and keep the same `event_id`, so ignore ids you have already handled.

## Errors

Every failure is an unchecked `G2BulkException` with `status()` (0 when no response arrived), `body()` (parsed JSON or text) and `headers()`:

| Class | When |
|---|---|
| `BadRequestException` | 400. `code()` is `insufficient_balance`, `out_of_stock`, `invalid_player`, `invalid_idempotency_key` or `null` |
| `AuthenticationException` | 401, missing or invalid key |
| `PermissionDeniedException` | 403, for example an IP your key does not allow |
| `NotFoundException` | 404 |
| `RateLimitException` | 429, `retryAfter()` as `Optional<Duration>` |
| `ServerException` | 5xx |
| `ConnectionException` | network failure or timeout |
| `WaitTimeoutException` | a `waitFor…` call ran out of time |
| `WebhookVerificationException` | a webhook failed verification |

```java
try {
    g2.purchases().create(12, PurchaseParams.builder().quantity(1).build());
} catch (BadRequestException e) {
    if (BadRequestException.INSUFFICIENT_BALANCE.equals(e.code())) notifyTopUpNeeded();
    else throw e;
}
```

## Retries and idempotency

GET requests, the read-only POSTs (`fields`, `servers`, `validatePlayer`, `eta`, `status`) and orders are retried up to `maxRetries` times on network errors, 429, 500, 502, 503 and 504, with exponential backoff and jitter that honours `Retry-After` (capped at 60 s). Nothing else is retried. Every order is sent with an `X-Idempotency-Key`, a random UUID generated for you unless you pass `idempotencyKey(...)`, and the same key is reused on every retry, so a retried order is never charged twice. Reuse your own 36-character key to make your retries safe too: a repeat within 30 minutes returns the original order.

## Development

No build tool is needed to work on the SDK:

```bash
./build.sh unit   # compile with javac --release 11 and run the unit tests
./build.sh e2e    # run the end-to-end tests against the API in ../e2e/.env.java
```
