package br.com.cashmobile.profile;

import java.time.Instant;

public record UserSettings(String name, String email, String phone, String office,
        boolean hideBalance, boolean reduceMotion, Instant updatedAt) {
    public static UserSettings defaults(Instant now) {
        return new UserSettings("Fulano de Lima", "fulano@example.com", "", "Cartório de demonstração", true, false, now);
    }
}
