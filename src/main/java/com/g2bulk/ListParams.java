package com.g2bulk;

public final class ListParams {
    private final Integer page;
    private final Integer limit;
    private final String search;

    private ListParams(Builder b) {
        this.page = b.page;
        this.limit = b.limit;
        this.search = b.search;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ListParams defaults() {
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

    public static final class Builder {
        private Integer page;
        private Integer limit;
        private String search;

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

        public ListParams build() {
            return new ListParams(this);
        }
    }
}
