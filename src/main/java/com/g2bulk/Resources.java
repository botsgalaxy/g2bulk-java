package com.g2bulk;

import java.util.List;
import java.util.Map;

final class Resources {
    private Resources() {
    }

    static Http.Request orderQuery(Http.Request req, OrderListParams p) {
        return req.query("page", p.page()).query("limit", p.limit()).query("search", p.search())
                .query("status", p.status()).query("from", p.from()).query("to", p.to())
                .query("tz", p.tz()).query("source", p.source()).query("summary", p.summary() ? "1" : null);
    }

    static Http.Request exportQuery(Http.Request req, ExportParams p) {
        return req.raw().query("from", p.from()).query("to", p.to()).query("search", p.search())
                .query("status", p.status()).query("tz", p.tz()).query("source", p.source());
    }

    static <T> Page<T> page(List<T> items, Map<String, Object> data) {
        OrderSummary summary = Json.has(data, "summary") ? new OrderSummary(Json.obj(data, "summary")) : null;
        return new Page<>(items, new Pagination(Json.obj(data, "pagination")), summary);
    }
}
