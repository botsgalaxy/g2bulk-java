package com.g2bulk;

import java.util.Map;

public final class Order {
    private final long id;
    private final long userId;
    private final long productId;
    private final String productTitle;
    private final int quantity;
    private final String totalPrice;
    private final String status;
    private final String description;
    private final String state;
    private final boolean refunded;
    private final String source;
    private final String deliveredAt;
    private final String createdAt;
    private final String updatedAt;

    Order(Map<String, Object> m) {
        this.id = Json.lng(m, "id");
        this.userId = Json.lng(m, "user_id");
        this.productId = Json.lng(m, "product_id");
        this.productTitle = Json.str(m, "product_title");
        this.quantity = Json.integer(m, "quantity");
        this.totalPrice = Json.str(m, "total_price");
        this.status = Json.str(m, "status");
        this.description = Json.str(m, "description");
        this.state = Json.str(m, "state");
        this.refunded = Json.bool(m, "is_refunded");
        this.source = Json.str(m, "source");
        this.deliveredAt = Json.str(m, "delivered_at");
        this.createdAt = Json.str(m, "created_at");
        this.updatedAt = Json.str(m, "updated_at");
    }

    public long id() {
        return id;
    }

    public long userId() {
        return userId;
    }

    public long productId() {
        return productId;
    }

    public String productTitle() {
        return productTitle;
    }

    public int quantity() {
        return quantity;
    }

    public String totalPrice() {
        return totalPrice;
    }

    public String status() {
        return status;
    }

    public String description() {
        return description;
    }

    public String state() {
        return state;
    }

    public boolean isRefunded() {
        return refunded;
    }

    public String source() {
        return source;
    }

    public String deliveredAt() {
        return deliveredAt;
    }

    public String createdAt() {
        return createdAt;
    }

    public String updatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Order{" + "id=" + id + ", " + "userId=" + userId + ", " + "productId=" + productId + ", " + "productTitle=" + productTitle + ", " + "quantity=" + quantity + ", " + "totalPrice=" + totalPrice + ", " + "status=" + status + ", " + "description=" + description + ", " + "state=" + state + ", " + "refunded=" + refunded + ", " + "source=" + source + ", " + "deliveredAt=" + deliveredAt + ", " + "createdAt=" + createdAt + ", " + "updatedAt=" + updatedAt + "}";
    }
}
