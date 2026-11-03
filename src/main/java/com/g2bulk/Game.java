package com.g2bulk;

import java.util.Map;

public final class Game {
    private final Long id;
    private final String code;
    private final String name;
    private final String imageUrl;

    Game(Map<String, Object> m) {
        this.id = Json.lngOrNull(m, "id");
        this.code = Json.str(m, "code");
        this.name = Json.str(m, "name");
        this.imageUrl = Json.str(m, "image_url");
    }

    public Long id() {
        return id;
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    public String imageUrl() {
        return imageUrl;
    }

    @Override
    public String toString() {
        return "Game{" + "id=" + id + ", " + "code=" + code + ", " + "name=" + name + ", " + "imageUrl=" + imageUrl + "}";
    }
}
