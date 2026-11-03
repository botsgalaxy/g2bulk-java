package com.g2bulk;

import java.util.Map;

public final class CatalogueItem {
    private final long id;
    private final String name;
    private final double amount;

    CatalogueItem(Map<String, Object> m) {
        this.id = Json.lng(m, "id");
        this.name = Json.str(m, "name");
        this.amount = Json.dbl(m, "amount");
    }

    public long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public double amount() {
        return amount;
    }

    @Override
    public String toString() {
        return "CatalogueItem{" + "id=" + id + ", " + "name=" + name + ", " + "amount=" + amount + "}";
    }
}
