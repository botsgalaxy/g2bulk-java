package com.g2bulk;

import java.util.Map;

public final class PlayerCheck {
    private final boolean valid;
    private final String name;
    private final String openid;

    PlayerCheck(Map<String, Object> m) {
        this.valid = Json.bool(m, "valid");
        this.name = Json.str(m, "name", "");
        this.openid = Json.str(m, "openid");
    }

    public boolean valid() {
        return valid;
    }

    public String name() {
        return name;
    }

    public String openid() {
        return openid;
    }

    @Override
    public String toString() {
        return "PlayerCheck{" + "valid=" + valid + ", " + "name=" + name + ", " + "openid=" + openid + "}";
    }
}
