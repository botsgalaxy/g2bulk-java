package com.g2bulk;

import java.util.List;
import java.util.Map;

public final class PurchaseResult {
    private final long orderId;
    private final long transactionId;
    private final long productId;
    private final String productTitle;
    private final String status;
    private final List<String> deliveryItems;
    private final String pollUrl;

    PurchaseResult(Map<String, Object> m) {
        this.orderId = Json.lng(m, "order_id");
        this.transactionId = Json.lng(m, "transaction_id");
        this.productId = Json.lng(m, "product_id");
        this.productTitle = Json.str(m, "product_title");
        this.status = Json.str(m, "status");
        this.deliveryItems = Json.strs(m, "delivery_items");
        this.pollUrl = Json.str(m, "poll_url");
    }

    public long orderId() {
        return orderId;
    }

    public long transactionId() {
        return transactionId;
    }

    public long productId() {
        return productId;
    }

    public String productTitle() {
        return productTitle;
    }

    public String status() {
        return status;
    }

    public List<String> deliveryItems() {
        return deliveryItems;
    }

    public String pollUrl() {
        return pollUrl;
    }

    @Override
    public String toString() {
        return "PurchaseResult{" + "orderId=" + orderId + ", " + "transactionId=" + transactionId + ", " + "productId=" + productId + ", " + "productTitle=" + productTitle + ", " + "status=" + status + ", " + "deliveryItems=" + deliveryItems + ", " + "pollUrl=" + pollUrl + "}";
    }
}
