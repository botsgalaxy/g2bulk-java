package com.g2bulk;

public final class OrderListParams {
    private final Integer page;
    private final Integer limit;
    private final String search;
    private final String status;
    private final String from;
    private final String to;
    private final String tz;
    private final String source;
    private final boolean summary;
    private final Long productId;
    private final String game;

    private OrderListParams(Builder b) {
        this.page = b.page;
        this.limit = b.limit;
        this.search = b.search;
        this.status = b.status;
        this.from = b.from;
        this.to = b.to;
        this.tz = b.tz;
        this.source = b.source;
        this.summary = b.summary;
        this.productId = b.productId;
        this.game = b.game;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static OrderListParams defaults() {
        return builder().build();
    }

    public Integer page() {
        return page;
    }

    public Integer limit() {
        return limit;
    }

    public String search() {
        return search;
    }

    public String status() {
        return status;
    }

    public String from() {
        return from;
    }

    public String to() {
        return to;
    }

    public String tz() {
        return tz;
    }

    public String source() {
        return source;
    }

    public boolean summary() {
        return summary;
    }

    public Long productId() {
        return productId;
    }

    public String game() {
        return game;
    }

    public static final class Builder {
        private Integer page;
        private Integer limit;
        private String search;
        private String status;
        private String from;
        private String to;
        private String tz;
        private String source;
        private boolean summary;
        private Long productId;
        private String game;

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

        public Builder search(String search) {
            this.search = search;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder tz(String tz) {
            this.tz = tz;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder summary(boolean summary) {
            this.summary = summary;
            return this;
        }

        public Builder productId(long productId) {
            this.productId = productId;
            return this;
        }

        public Builder game(String game) {
            this.game = game;
            return this;
        }

        public OrderListParams build() {
            return new OrderListParams(this);
        }
    }
}
