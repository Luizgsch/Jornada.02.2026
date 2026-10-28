package br.com.cashmobile.profile;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final ProfileRepository repository;
    private final Clock clock;
    public ProfileService(ProfileRepository repository, Clock clock) { this.repository = repository; this.clock = clock; }

    public ProfileView get(String owner) { return view(owner, repository.get(owner)); }
    public ProfileView update(String owner, ProfileInput input) {
        var changed = repository.change(owner, current -> new UserSettings(input.name().strip(), input.email().strip(),
                input.phone() == null ? "" : input.phone().strip(), input.office().strip(),
                current.hideBalance(), current.reduceMotion(), clock.instant()));
        return view(owner, changed);
    }
    public Preferences preferences(String owner) {
        var current = repository.get(owner);
        return new Preferences(current.hideBalance(), current.reduceMotion());
    }
    public Preferences preferences(String owner, boolean hideBalance, boolean reduceMotion) {
        var changed = repository.change(owner, current -> new UserSettings(current.name(), current.email(), current.phone(),
                current.office(), hideBalance, reduceMotion, clock.instant()));
        return new Preferences(changed.hideBalance(), changed.reduceMotion());
    }
    private ProfileView view(String owner, UserSettings settings) {
        return new ProfileView(true, owner, "DEMO", settings.name(), settings.email(), settings.phone(), settings.office(), settings.updatedAt());
    }
    public record ProfileView(boolean demonstration, String loginEmail, String role, String name, String email,
            String phone, String office, Instant updatedAt) {}
    public record Preferences(boolean hideBalance, boolean reduceMotion) {}
}
