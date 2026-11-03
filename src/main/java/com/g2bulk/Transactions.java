package com.g2bulk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Transactions {
    private final Http http;

    Transactions(Http http) {
        this.http = http;
    }

    public Page<Transaction> list() {
        return list(ListParams.defaults());
    }

    public Page<Transaction> list(ListParams params) {
        Http.Request req = Http.Request.get("/v1/transactions")
                .query("page", params.page()).query("limit", params.limit()).query("search", params.search());
        Map<String, Object> data = http.send(req).obj();
        List<Transaction> items = new ArrayList<>();
        for (Map<String, Object> o : Json.objs(data, "data")) items.add(new Transaction(o));
        return Resources.page(items, data);
    }
}
