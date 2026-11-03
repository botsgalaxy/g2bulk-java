package com.g2bulk;

import java.util.Map;

public final class GameOrder {
    private final long orderId;
    private final String gameCode;
    private final String gameName;
    private final String playerId;
    private final String playerName;
    private final String serverId;
    private final String denomId;
    private final double price;
    private final String status;
    private final boolean refunded;
    private final String remark;
    private final String message;
    private final String callbackUrl;
    private final String state;
    private final String source;
    private final String createdAt;
    private final String updatedAt;
    private final String completedAt;

    GameOrder(Map<String, Object> m) {
        this.orderId = Json.lng(m, "order_id");
        this.gameCode = Json.str(m, "game_code");
        this.gameName = Json.str(m, "game_name");
        this.playerId = Json.str(m, "player_id");
        this.playerName = Json.str(m, "player_name");
        this.serverId = Json.str(m, "server_id");
        this.denomId = Json.str(m, "denom_id");
        this.price = Json.dbl(m, "price");
        this.status = Json.str(m, "status");
        this.refunded = Json.bool(m, "is_refunded");
        this.remark = Json.str(m, "remark");
        this.message = Json.str(m, "message");
        this.callbackUrl = Json.str(m, "callback_url");
        this.state = Json.str(m, "state");
        this.source = Json.str(m, "source");
        this.createdAt = Json.str(m, "created_at");
        this.updatedAt = Json.str(m, "updated_at");
        this.completedAt = Json.str(m, "completed_at");
    }

    public long orderId() {
        return orderId;
    }

    public String gameCode() {
        return gameCode;
    }

    public String gameName() {
        return gameName;
    }

    public String playerId() {
        return playerId;
    }

    public String playerName() {
        return playerName;
    }

    public String serverId() {
        return serverId;
    }

    public String denomId() {
        return denomId;
    }

    public double price() {
        return price;
    }

    public String status() {
        return status;
    }

    public boolean isRefunded() {
        return refunded;
    }

    public String remark() {
        return remark;
    }

    public String message() {
        return message;
    }

    public String callbackUrl() {
        return callbackUrl;
    }

    public String state() {
        return state;
    }

    public String source() {
        return source;
    }

    public String createdAt() {
        return createdAt;
    }

    public String updatedAt() {
        return updatedAt;
    }

    public String completedAt() {
        return completedAt;
    }

    @Override
    public String toString() {
        return "GameOrder{" + "orderId=" + orderId + ", " + "gameCode=" + gameCode + ", " + "gameName=" + gameName + ", " + "playerId=" + playerId + ", " + "playerName=" + playerName + ", " + "serverId=" + serverId + ", " + "denomId=" + denomId + ", " + "price=" + price + ", " + "status=" + status + ", " + "refunded=" + refunded + ", " + "remark=" + remark + ", " + "message=" + message + ", " + "callbackUrl=" + callbackUrl + ", " + "state=" + state + ", " + "source=" + source + ", " + "createdAt=" + createdAt + ", " + "updatedAt=" + updatedAt + ", " + "completedAt=" + completedAt + "}";
    }
}
