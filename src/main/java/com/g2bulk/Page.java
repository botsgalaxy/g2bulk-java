package com.g2bulk;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class Page<T> {
    private final List<T> items;
    private final Pagination pagination;
    private final OrderSummary summary;

    Page(List<T> items, Pagination pagination, OrderSummary summary) {
        this.items = Collections.unmodifiableList(items);
        this.pagination = pagination;
        this.summary = summary;
    }

    public List<T> items() {
        return items;
    }

    public Pagination pagination() {
        return pagination;
    }

    public Optional<OrderSummary> summary() {
        return Optional.ofNullable(summary);
    }

    @Override
    public String toString() {
        return "Page{items=" + items + ", pagination=" + pagination + ", summary=" + summary + "}";
    }
}
