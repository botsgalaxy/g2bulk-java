package com.g2bulk;

public final class Account {
    private final Http http;

    Account(Http http) {
        this.http = http;
    }

    public User me() {
        return new User(http.send(Http.Request.get("/v1/getMe")).obj());
    }
}
