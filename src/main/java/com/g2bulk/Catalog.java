package com.g2bulk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class Catalog {
    private final Http http;

    Catalog(Http http) {
        this.http = http;
    }

    public List<Category> categories() {
        Map<String, Object> data = http.send(Http.Request.get("/v1/category").publicEndpoint()).obj();
        List<Category> out = new ArrayList<>();
        for (Map<String, Object> o : Json.objs(data, "categories")) out.add(new Category(o));
        return Collections.unmodifiableList(out);
    }

    public List<Product> categoryProducts(long categoryId) {
        return products(http.send(Http.Request.get("/v1/category/" + categoryId).publicEndpoint()).obj());
    }

    public List<Product> products() {
        return products(http.send(Http.Request.get("/v1/products").publicEndpoint()).obj());
    }

    public Product product(long productId) {
        Map<String, Object> data = http.send(Http.Request.get("/v1/products/" + productId).publicEndpoint()).obj();
        return new Product(Json.obj(data, "product"));
    }

    private static List<Product> products(Map<String, Object> data) {
        List<Product> out = new ArrayList<>();
        for (Map<String, Object> o : Json.objs(data, "products")) out.add(new Product(o));
        return Collections.unmodifiableList(out);
    }
}
