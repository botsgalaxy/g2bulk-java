package com.g2bulk;

import java.util.Map;

public final class Product {
    private final long id;
    private final String title;
    private final String description;
    private final long categoryId;
    private final String categoryTitle;
    private final double unitPrice;
    private final Double faceValue;
    private final String imageUrl;
    private final long stock;

    Product(Map<String, Object> m) {
        this.id = Json.lng(m, "id");
        this.title = Json.str(m, "title");
        this.description = Json.str(m, "description");
        this.categoryId = Json.lng(m, "category_id");
        this.categoryTitle = Json.str(m, "category_title");
        this.unitPrice = Json.dbl(m, "unit_price");
        this.faceValue = Json.dblOrNull(m, "face_value");
        this.imageUrl = Json.str(m, "image_url");
        this.stock = Json.lng(m, "stock");
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

    public long categoryId() {
        return categoryId;
    }

    public String categoryTitle() {
        return categoryTitle;
    }

    public double unitPrice() {
        return unitPrice;
    }

    public Double faceValue() {
        return faceValue;
    }

    public String imageUrl() {
        return imageUrl;
    }

    public long stock() {
        return stock;
    }

    @Override
    public String toString() {
        return "Product{" + "id=" + id + ", " + "title=" + title + ", " + "description=" + description + ", " + "categoryId=" + categoryId + ", " + "categoryTitle=" + categoryTitle + ", " + "unitPrice=" + unitPrice + ", " + "faceValue=" + faceValue + ", " + "imageUrl=" + imageUrl + ", " + "stock=" + stock + "}";
    }
}
