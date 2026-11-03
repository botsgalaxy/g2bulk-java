package com.g2bulk;

import java.util.Map;

public final class GameOrderSummary {
    private final long orderId;
    private final String game;
    private final String catalogue;
    private final String playerId;
    private final String playerName;
    private final String serverId;
    private final double price;
    private final String status;
    private final String remark;
    private final String callbackUrl;

    GameOrderSummary(Map<String, Object> m) {
        this.orderId = Json.lng(m, "order_id");
        this.game = Json.str(m, "game");
        this.catalogue = Json.str(m, "catalogue");
        this.playerId = Json.str(m, "player_id");
        this.playerName = Json.str(m, "player_name");
        this.serverId = Json.str(m, "server_id");
        this.price = Json.dbl(m, "price");
        this.status = Json.str(m, "status");
        this.remark = Json.str(m, "remark");
        this.callbackUrl = Json.str(m, "callback_url");
    }

    public long orderId() {
        return orderId;
    }

    public String game() {
        return game;
    }

    public String catalogue() {
        return catalogue;
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

    public double price() {
        return price;
    }

    public String status() {
        return status;
    }

    public String remark() {
        return remark;
    }

    public String callbackUrl() {
        return callbackUrl;
    }

    @Override
    public String toString() {
        return "GameOrderSummary{" + "orderId=" + orderId + ", " + "game=" + game + ", " + "catalogue=" + catalogue + ", " + "playerId=" + playerId + ", " + "playerName=" + playerName + ", " + "serverId=" + serverId + ", " + "price=" + price + ", " + "status=" + status + ", " + "remark=" + remark + ", " + "callbackUrl=" + callbackUrl + "}";
    }
}
