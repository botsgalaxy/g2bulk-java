package com.g2bulk;

import java.util.Map;

public final class OrderSummary {
    private final long orders;
    private final long completed;
    private final long inProgress;
    private final long failed;
    private final long cancelled;
    private final double spend;
    private final double refunded;

    OrderSummary(Map<String, Object> m) {
        this.orders = Json.lng(m, "orders");
        this.completed = Json.lng(m, "completed");
        this.inProgress = Json.lng(m, "in_progress");
        this.failed = Json.lng(m, "failed");
        this.cancelled = Json.lng(m, "cancelled");
        this.spend = Json.dbl(m, "spend");
        this.refunded = Json.dbl(m, "refunded");
    }

    public long orders() {
        return orders;
    }

    public long completed() {
        return completed;
    }

    public long inProgress() {
        return inProgress;
    }

    public long failed() {
        return failed;
    }

    public long cancelled() {
        return cancelled;
    }

    public double spend() {
        return spend;
    }

    public double refunded() {
        return refunded;
    }

    @Override
    public String toString() {
        return "OrderSummary{" + "orders=" + orders + ", " + "completed=" + completed + ", " + "inProgress=" + inProgress + ", " + "failed=" + failed + ", " + "cancelled=" + cancelled + ", " + "spend=" + spend + ", " + "refunded=" + refunded + "}";
    }
}
