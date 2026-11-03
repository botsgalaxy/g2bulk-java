package com.g2bulk;

import java.util.Map;

public final class Eta {
    private final String label;
    private final String display;
    private final Double medianSeconds;

    Eta(Map<String, Object> m) {
        this.label = Json.str(m, "label");
        this.display = Json.str(m, "display");
        this.medianSeconds = Json.dblOrNull(m, "median_seconds");
    }

    public String label() {
        return label;
    }

    public String display() {
        return display;
    }

    public Double medianSeconds() {
        return medianSeconds;
    }

    @Override
    public String toString() {
        return "Eta{" + "label=" + label + ", " + "display=" + display + ", " + "medianSeconds=" + medianSeconds + "}";
    }
}
