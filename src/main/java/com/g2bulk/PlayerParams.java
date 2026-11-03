package com.g2bulk;

public final class PlayerParams {
    private final String game;
    private final String userId;
    private final String serverId;
    private final String charname;

    private PlayerParams(Builder b) {
        this.game = b.game;
        this.userId = b.userId;
        this.serverId = b.serverId;
        this.charname = b.charname;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String game() {
        return game;
    }

    public String userId() {
        return userId;
    }

    public String serverId() {
        return serverId;
    }

    public String charname() {
        return charname;
    }

    public static final class Builder {
        private String game;
        private String userId;
        private String serverId;
        private String charname;

        private Builder() {
        }

        public Builder game(String game) {
            this.game = game;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder serverId(String serverId) {
            this.serverId = serverId;
            return this;
        }

        public Builder charname(String charname) {
            this.charname = charname;
            return this;
        }

        public PlayerParams build() {
            if (game == null) throw new IllegalStateException("game is required");
            if (userId == null) throw new IllegalStateException("userId is required");
            return new PlayerParams(this);
        }
    }
}
