package com.g2bulk;

import java.util.List;
import java.util.Map;

public final class Delivery {
    public enum State { READY, PROCESSING, FAILED }

    private final long orderId;
    private final Long productId;
    private final String productTitle;
    private final Integer quantity;
    private final String status;
    private final List<String> deliveryItems;
    private final boolean refunded;
    private final String message;
    private final State state;

    Delivery(Map<String, Object> m, int httpStatus) {
        this.orderId = Json.lng(m, "order_id");
        this.productId = Json.lngOrNull(m, "product_id");
        this.productTitle = Json.str(m, "product_title");
        Long q = Json.lngOrNull(m, "quantity");
        this.quantity = q == null ? null : q.intValue();
        this.status = Json.str(m, "status");
        this.deliveryItems = Json.strs(m, "delivery_items");
        this.refunded = Json.bool(m, "refunded");
        this.message = Json.str(m, "message");
        this.state = httpStatus == 200 ? State.READY : httpStatus == 202 ? State.PROCESSING : State.FAILED;
    }

    public long orderId() {
        return orderId;
    }

    public Long productId() {
        return productId;
    }

    public String productTitle() {
        return productTitle;
    }

    public Integer quantity() {
        return quantity;
    }

    public String status() {
        return status;
    }

    public List<String> deliveryItems() {
        return deliveryItems;
    }

    public boolean refunded() {
        return refunded;
    }

    public String message() {
        return message;
    }

    public State state() {
        return state;
    }

    @Override
    public String toString() {
        return "Delivery{orderId=" + orderId + ", status=" + status + ", state=" + state + ", items=" + deliveryItems.size() + "}";
    }
}
