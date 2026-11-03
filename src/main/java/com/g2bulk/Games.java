package com.g2bulk;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Games {
    private final Http http;

    Games(Http http) {
        this.http = http;
    }

    public List<Game> list() {
        Map<String, Object> data = http.send(Http.Request.get("/v1/games").publicEndpoint()).obj();
        List<Game> out = new ArrayList<>();
        for (Map<String, Object> o : Json.objs(data, "games")) out.add(new Game(o));
        return Collections.unmodifiableList(out);
    }

    public GameFields fields(String game) {
        Http.Request req = Http.Request.post("/v1/games/fields").body(Http.body("game", game)).publicEndpoint().retry();
        return new GameFields(Json.obj(http.send(req).obj(), "info"));
    }

    public Optional<Map<String, String>> servers(String game) {
        Http.Request req = Http.Request.post("/v1/games/servers").body(Http.body("game", game)).publicEndpoint().retry();
        try {
            Map<String, Object> servers = Json.obj(http.send(req).obj(), "servers");
            Map<String, String> out = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : servers.entrySet()) out.put(e.getKey(), Json.str(servers, e.getKey()));
            return Optional.of(Collections.unmodifiableMap(out));
        } catch (PermissionDeniedException e) {
            return Optional.empty();
        }
    }

    public PlayerCheck validatePlayer(PlayerParams params) {
        Http.Request req = Http.Request.post("/v1/games/checkPlayerId")
                .body(Http.body("game", params.game(), "user_id", params.userId(), "server_id", params.serverId(), "charname", params.charname()))
                .publicEndpoint().retry().expect(400);
        Http.Response res = http.send(req);
        Map<String, Object> data = res.obj();
        if (!(data.get("valid") instanceof String)) {
            String message = res.status == 400 ? Http.messageOf(res.data, res.text, 400) : "Player check failed";
            throw new BadRequestException(message, 400, res.data, res.headers);
        }
        Map<String, Object> normalized = new LinkedHashMap<>(data);
        normalized.put("valid", "valid".equals(data.get("valid")));
        return new PlayerCheck(normalized);
    }

    public GameCatalogue catalogue(String game) {
        Http.Request req = Http.Request.get("/v1/games/" + Http.encode(game) + "/catalogue").publicEndpoint();
        return new GameCatalogue(http.send(req).obj());
    }

    public Eta eta(String game, String catalogueName) {
        Http.Request req = Http.Request.post("/v1/games/eta")
                .body(Http.body("game_code", game, "denom_id", catalogueName)).publicEndpoint().retry();
        return new Eta(Json.obj(http.send(req).obj(), "estimated_time"));
    }

    public GameOrderSummary order(String game, GameOrderParams params) {
        String key = params.idempotencyKey() != null ? params.idempotencyKey() : Http.newIdempotencyKey();
        Http.Request req = Http.Request.post("/v1/games/" + Http.encode(game) + "/order")
                .body(Http.body(
                        "catalogue_name", params.catalogueName(),
                        "player_id", params.playerId(),
                        "server_id", params.serverId(),
                        "charname", params.charname(),
                        "remark", params.remark(),
                        "callback_url", params.callbackUrl()))
                .idempotencyKey(key);
        return new GameOrderSummary(Json.obj(http.send(req).obj(), "order"));
    }

    public GameOrderSummary status(long orderId, String game) {
        Http.Request req = Http.Request.post("/v1/games/order/status")
                .body(Http.body("order_id", orderId, "game", game)).retry();
        return new GameOrderSummary(Json.obj(http.send(req).obj(), "order"));
    }

    public Page<GameOrder> listOrders() {
        return listOrders(OrderListParams.defaults());
    }

    public Page<GameOrder> listOrders(OrderListParams params) {
        Http.Request req = Resources.orderQuery(Http.Request.get("/v1/games/orders"), params).query("game", params.game());
        Map<String, Object> data = http.send(req).obj();
        List<GameOrder> items = new ArrayList<>();
        for (Map<String, Object> o : Json.objs(data, "orders")) items.add(new GameOrder(o));
        return Resources.page(items, data);
    }

    public GameOrder getOrder(long orderId) {
        return new GameOrder(Json.obj(http.send(Http.Request.get("/v1/games/orders/" + orderId)).obj(), "order"));
    }

    public GameOrder waitForOrder(long orderId) {
        return waitForOrder(orderId, Duration.ofSeconds(5), Duration.ofMinutes(10));
    }

    public GameOrder waitForOrder(long orderId, Duration interval, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (true) {
            GameOrder order = getOrder(orderId);
            if ("COMPLETED".equals(order.status()) || "FAILED".equals(order.status())) return order;
            if (System.nanoTime() + interval.toNanos() > deadline) {
                throw new WaitTimeoutException("Order " + orderId + " is still " + order.status());
            }
            Http.sleep(interval.toMillis());
        }
    }

    public String exportCsv(ExportParams params) {
        Http.Request req = Resources.exportQuery(Http.Request.get("/v1/games/orders/export.csv"), params).query("game", params.game());
        return http.send(req).text;
    }
}
