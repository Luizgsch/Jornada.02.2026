package br.com.cashmobile.profile;

import java.util.function.UnaryOperator;

public interface ProfileRepository {
    UserSettings get(String owner);
    UserSettings change(String owner, UnaryOperator<UserSettings> update);
}
