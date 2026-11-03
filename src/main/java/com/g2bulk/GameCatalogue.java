package com.g2bulk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class GameCatalogue {
    private final Game game;
    private final List<CatalogueItem> catalogues;

    GameCatalogue(Map<String, Object> m) {
        this.game = new Game(Json.obj(m, "game"));
        List<CatalogueItem> items = new ArrayList<>();
        for (Map<String, Object> o : Json.objs(m, "catalogues")) items.add(new CatalogueItem(o));
        this.catalogues = Collections.unmodifiableList(items);
    }

    public Game game() {
        return game;
    }

    public List<CatalogueItem> catalogues() {
        return catalogues;
    }

    @Override
    public String toString() {
        return "GameCatalogue{game=" + game + ", catalogues=" + catalogues + "}";
    }
}
