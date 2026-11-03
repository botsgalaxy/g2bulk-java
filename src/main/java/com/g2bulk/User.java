package com.g2bulk;

import java.util.Map;

public final class User {
    private final long userId;
    private final Long telegramId;
    private final String email;
    private final boolean emailVerified;
    private final String firstName;
    private final String username;
    private final double balance;
    private final String twoFactorMethod;
    private final String createdAt;

    User(Map<String, Object> m) {
        this.userId = Json.lng(m, "user_id");
        this.telegramId = Json.lngOrNull(m, "telegram_id");
        this.email = Json.str(m, "email");
        this.emailVerified = Json.bool(m, "email_verified");
        this.firstName = Json.str(m, "first_name");
        this.username = Json.str(m, "username");
        this.balance = Json.dbl(m, "balance");
        this.twoFactorMethod = Json.str(m, "two_factor_method");
        this.createdAt = Json.str(m, "created_at");
    }

    public long userId() {
        return userId;
    }

    public Long telegramId() {
        return telegramId;
    }

    public String email() {
        return email;
    }

    public boolean emailVerified() {
        return emailVerified;
    }

    public String firstName() {
        return firstName;
    }

    public String username() {
        return username;
    }

    public double balance() {
        return balance;
    }

    public String twoFactorMethod() {
        return twoFactorMethod;
    }

    public String createdAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "User{" + "userId=" + userId + ", " + "telegramId=" + telegramId + ", " + "email=" + email + ", " + "emailVerified=" + emailVerified + ", " + "firstName=" + firstName + ", " + "username=" + username + ", " + "balance=" + balance + ", " + "twoFactorMethod=" + twoFactorMethod + ", " + "createdAt=" + createdAt + "}";
    }
}
