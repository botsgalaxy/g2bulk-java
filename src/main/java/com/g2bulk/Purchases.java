package com.g2bulk;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Purchases {
    private final Http http;

    Purchases(Http http) {
        this.http = http;
    }

    public PurchaseResult create(long productId, PurchaseParams params) {
        String key = params.idempotencyKey() != null ? params.idempotencyKey() : Http.newIdempotencyKey();
        Http.Request req = Http.Request.post("/v1/products/" + productId + "/purchase")
                .body(Http.body("quantity", params.quantity(), "description", params.description(), "callback_url", params.callbackUrl()))
                .idempotencyKey(key);
        return new PurchaseResult(http.send(req).obj());
    }

    public Page<Order> list() {
        return list(OrderListParams.defaults());
    }

    public Page<Order> list(OrderListParams params) {
        Http.Request req = Resources.orderQuery(Http.Request.get("/v1/orders"), params).query("product_id", params.productId());
        Map<String, Object> data = http.send(req).obj();
        List<Order> items = new ArrayList<>();
        for (Map<String, Object> o : Json.objs(data, "orders")) items.add(new Order(o));
        return Resources.page(items, data);
    }

    public Order get(long orderId) {
        return new Order(Json.obj(http.send(Http.Request.get("/v1/orders/" + orderId)).obj(), "order"));
    }

    public Delivery delivery(long orderId) {
        Http.Response res = http.send(Http.Request.get("/v1/orders/" + orderId + "/delivery").expect(202, 410));
        return new Delivery(res.obj(), res.status);
    }

    public Delivery waitForDelivery(long orderId) {
        return waitForDelivery(orderId, Duration.ofSeconds(3), Duration.ofMinutes(2));
    }

    public Delivery waitForDelivery(long orderId, Duration interval, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (true) {
            Delivery delivery = delivery(orderId);
            if (delivery.state() != Delivery.State.PROCESSING) return delivery;
            if (System.nanoTime() + interval.toNanos() > deadline) {
                throw new WaitTimeoutException("Order " + orderId + " is still processing");
            }
            Http.sleep(interval.toMillis());
        }
    }

    public String exportCsv(ExportParams params) {
        Http.Request req = Resources.exportQuery(Http.Request.get("/v1/orders/export.csv"), params).query("product_id", params.productId());
        return http.send(req).text;
    }
}
