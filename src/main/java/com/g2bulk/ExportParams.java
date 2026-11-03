package com.g2bulk;

public final class ExportParams {
    private final String from;
    private final String to;
    private final String search;
    private final String status;
    private final String tz;
    private final String source;
    private final Long productId;
    private final String game;

    private ExportParams(Builder b) {
        this.from = b.from;
        this.to = b.to;
        this.search = b.search;
        this.status = b.status;
        this.tz = b.tz;
        this.source = b.source;
        this.productId = b.productId;
        this.game = b.game;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String from() {
        return from;
    }

    public String to() {
        return to;
    }

    public String search() {
        return search;
    }

    public String status() {
        return status;
    }

    public String tz() {
        return tz;
    }

    public String source() {
        return source;
    }

    public Long productId() {
        return productId;
    }

    public String game() {
        return game;
    }

    public static final class Builder {
        private String from;
        private String to;
        private String search;
        private String status;
        private String tz;
        private String source;
        private Long productId;
        private String game;

        private Builder() {
        }

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
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

        public Builder tz(String tz) {
            this.tz = tz;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
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

        public ExportParams build() {
            if (from == null) throw new IllegalStateException("from is required");
            if (to == null) throw new IllegalStateException("to is required");
            return new ExportParams(this);
        }
    }
}
