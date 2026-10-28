package br.com.cashmobile.profile;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryProfileRepository implements ProfileRepository {
    private final Map<String, UserSettings> profiles = new HashMap<>();
    private final Clock clock;
    public InMemoryProfileRepository(Clock clock) { this.clock = clock; }

    @Override
    public synchronized UserSettings get(String owner) {
        return profiles.computeIfAbsent(owner, key -> UserSettings.defaults(clock.instant()));
    }

    @Override
    public synchronized UserSettings change(String owner, UnaryOperator<UserSettings> update) {
        var changed = update.apply(get(owner));
        profiles.put(owner, changed);
        return changed;
    }
}
