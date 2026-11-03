package com.g2bulk;

public final class GameOrderParams {
    private final String catalogueName;
    private final String playerId;
    private final String serverId;
    private final String charname;
    private final String remark;
    private final String callbackUrl;
    private final String idempotencyKey;

    private GameOrderParams(Builder b) {
        this.catalogueName = b.catalogueName;
        this.playerId = b.playerId;
        this.serverId = b.serverId;
        this.charname = b.charname;
        this.remark = b.remark;
        this.callbackUrl = b.callbackUrl;
        this.idempotencyKey = b.idempotencyKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String catalogueName() {
        return catalogueName;
    }

    public String playerId() {
        return playerId;
    }

    public String serverId() {
        return serverId;
    }

    public String charname() {
        return charname;
    }

    public String remark() {
        return remark;
    }

    public String callbackUrl() {
        return callbackUrl;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    public static final class Builder {
        private String catalogueName;
        private String playerId;
        private String serverId;
        private String charname;
        private String remark;
        private String callbackUrl;
        private String idempotencyKey;

        private Builder() {
        }

        public Builder catalogueName(String catalogueName) {
            this.catalogueName = catalogueName;
            return this;
        }

        public Builder playerId(String playerId) {
            this.playerId = playerId;
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

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public Builder callbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public GameOrderParams build() {
            if (catalogueName == null) throw new IllegalStateException("catalogueName is required");
            if (playerId == null) throw new IllegalStateException("playerId is required");
            return new GameOrderParams(this);
        }
    }
}
