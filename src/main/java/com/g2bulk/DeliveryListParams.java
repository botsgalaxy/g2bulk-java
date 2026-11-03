package com.g2bulk;

public final class DeliveryListParams {
    private final Integer page;
    private final Integer limit;
    private final String status;
    private final Long orderId;

    private DeliveryListParams(Builder b) {
        this.page = b.page;
        this.limit = b.limit;
        this.status = b.status;
        this.orderId = b.orderId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DeliveryListParams defaults() {
        return builder().build();
    }

    public Integer page() {
        return page;
    }

    public Integer limit() {
        return limit;
    }

    public String status() {
        return status;
    }

    public Long orderId() {
        return orderId;
    }

    public static final class Builder {
        private Integer page;
        private Integer limit;
        private String status;
        private Long orderId;

        private Builder() {
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder orderId(long orderId) {
            this.orderId = orderId;
            return this;
        }

        public DeliveryListParams build() {
            return new DeliveryListParams(this);
        }
    }
}
