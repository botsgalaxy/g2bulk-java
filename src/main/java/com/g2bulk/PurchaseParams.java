package com.g2bulk;

public final class PurchaseParams {
    private final int quantity;
    private final String description;
    private final String callbackUrl;
    private final String idempotencyKey;

    private PurchaseParams(Builder b) {
        this.quantity = b.quantity;
        this.description = b.description;
        this.callbackUrl = b.callbackUrl;
        this.idempotencyKey = b.idempotencyKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static PurchaseParams defaults() {
        return builder().build();
    }

    public int quantity() {
        return quantity;
    }

    public String description() {
        return description;
    }

    public String callbackUrl() {
        return callbackUrl;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    public static final class Builder {
        private int quantity = 1;
        private String description;
        private String callbackUrl;
        private String idempotencyKey;

        private Builder() {
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder callbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public PurchaseParams build() {
            if (quantity < 1) throw new IllegalStateException("quantity must be at least 1");
            return new PurchaseParams(this);
        }
    }
}
