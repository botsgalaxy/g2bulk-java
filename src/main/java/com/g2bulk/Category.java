package com.g2bulk;

import java.util.Map;

public final class Category {
    private final long id;
    private final String title;
    private final String description;
    private final String imageUrl;
    private final int productCount;

    Category(Map<String, Object> m) {
        this.id = Json.lng(m, "id");
        this.title = Json.str(m, "title");
        this.description = Json.str(m, "description");
        this.imageUrl = Json.str(m, "image_url");
        this.productCount = Json.integer(m, "product_count");
    }

    public long id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String imageUrl() {
        return imageUrl;
    }

    public int productCount() {
        return productCount;
    }

    @Override
    public String toString() {
        return "Category{" + "id=" + id + ", " + "title=" + title + ", " + "description=" + description + ", " + "imageUrl=" + imageUrl + ", " + "productCount=" + productCount + "}";
    }
}
