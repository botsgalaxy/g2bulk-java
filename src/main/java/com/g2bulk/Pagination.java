package com.g2bulk;

import java.util.Map;

public final class Pagination {
    private final int page;
    private final int limit;
    private final long total;
    private final int totalPages;

    Pagination(Map<String, Object> m) {
        this.page = Json.integer(m, "page");
        this.limit = Json.integer(m, "limit");
        this.total = Json.lng(m, "total");
        this.totalPages = Json.integer(m, "total_pages");
    }

    public int page() {
        return page;
    }

    public int limit() {
        return limit;
    }

    public long total() {
        return total;
    }

    public int totalPages() {
        return totalPages;
    }

    @Override
    public String toString() {
        return "Pagination{" + "page=" + page + ", " + "limit=" + limit + ", " + "total=" + total + ", " + "totalPages=" + totalPages + "}";
    }
}
