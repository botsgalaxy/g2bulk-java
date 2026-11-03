package com.g2bulk;

import java.time.Duration;

public final class G2Bulk {
    public static final String VERSION = Http.VERSION;
    public static final String DEFAULT_BASE_URL = "https://api.g2bulk.com";

    private final Account account;
    private final Catalog catalog;
    private final Purchases purchases;
    private final Transactions transactions;
    private final Games games;
    private final Webhooks webhooks;

    private G2Bulk(Http http) {
        this.account = new Account(http);
        this.catalog = new Catalog(http);
        this.purchases = new Purchases(http);
        this.transactions = new Transactions(http);
        this.games = new Games(http);
        this.webhooks = new Webhooks(http);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static G2Bulk fromEnv() {
        return builder().build();
    }

    public Account account() {
        return account;
    }

    public Catalog catalog() {
        return catalog;
    }

    public Purchases purchases() {
        return purchases;
    }

    public Transactions transactions() {
        return transactions;
    }

    public Games games() {
        return games;
    }

    public Webhooks webhooks() {
        return webhooks;
    }

    public static final class Builder {
        private String apiKey;
        private String baseUrl;
        private Duration timeout = Duration.ofSeconds(30);
        private int maxRetries = 2;
        private Transport transport;

        private Builder() {
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder timeout(Duration timeout) {
            if (timeout == null || timeout.isNegative() || timeout.isZero()) throw new IllegalArgumentException("timeout must be positive");
            this.timeout = timeout;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            if (maxRetries < 0) throw new IllegalArgumentException("maxRetries must not be negative");
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder transport(Transport transport) {
            this.transport = transport;
            return this;
        }

        public G2Bulk build() {
            String key = apiKey != null ? apiKey : System.getenv("G2BULK_API_KEY");
            String url = baseUrl;
            if (url == null || url.isEmpty()) url = System.getenv("G2BULK_BASE_URL");
            if (url == null || url.isEmpty()) url = DEFAULT_BASE_URL;
            Transport t = transport != null ? transport : new JdkTransport(timeout);
            return new G2Bulk(new Http(key, url, timeout, maxRetries, t));
        }
    }
}
