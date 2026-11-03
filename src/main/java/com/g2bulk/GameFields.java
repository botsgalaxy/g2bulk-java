package com.g2bulk;

import java.util.List;
import java.util.Map;

public final class GameFields {
    private final List<String> fields;
    private final String notes;

    GameFields(Map<String, Object> m) {
        this.fields = Json.strs(m, "fields");
        this.notes = Json.str(m, "notes", "");
    }

    public List<String> fields() {
        return fields;
    }

    public String notes() {
        return notes;
    }

    @Override
    public String toString() {
        return "GameFields{" + "fields=" + fields + ", " + "notes=" + notes + "}";
    }
}
