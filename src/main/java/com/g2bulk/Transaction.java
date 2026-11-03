package com.g2bulk;

import java.util.Map;

public final class Transaction {
    private final long id;
    private final long userId;
    private final String transactionType;
    private final String amount;
    private final String balanceBefore;
    private final String balanceAfter;
    private final String status;
    private final String description;
    private final String createdAt;

    Transaction(Map<String, Object> m) {
        this.id = Json.lng(m, "id");
        this.userId = Json.lng(m, "user_id");
        this.transactionType = Json.str(m, "transaction_type");
        this.amount = Json.str(m, "amount");
        this.balanceBefore = Json.str(m, "balance_before");
        this.balanceAfter = Json.str(m, "balance_after");
        this.status = Json.str(m, "status");
        this.description = Json.str(m, "description");
        this.createdAt = Json.str(m, "created_at");
    }

    public long id() {
        return id;
    }

    public long userId() {
        return userId;
    }

    public String transactionType() {
        return transactionType;
    }

    public String amount() {
        return amount;
    }

    public String balanceBefore() {
        return balanceBefore;
    }

    public String balanceAfter() {
        return balanceAfter;
    }

    public String status() {
        return status;
    }

    public String description() {
        return description;
    }

    public String createdAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Transaction{" + "id=" + id + ", " + "userId=" + userId + ", " + "transactionType=" + transactionType + ", " + "amount=" + amount + ", " + "balanceBefore=" + balanceBefore + ", " + "balanceAfter=" + balanceAfter + ", " + "status=" + status + ", " + "description=" + description + ", " + "createdAt=" + createdAt + "}";
    }
}
