package com.maris7.settings.api;

import com.maris7.settings.MarisSettingsPlugin;
import com.maris7.settings.model.SettingFeature;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface MarisSettingsApi {
    boolean isEnabled(UUID uuid, SettingFeature feature);

    default boolean isEnabled(Player player, SettingFeature feature) {
        return isEnabled(player.getUniqueId(), feature);
    }

    boolean isEnabled(UUID uuid, String feature, boolean defaultValue);

    boolean set(UUID uuid, String feature, boolean enabled);

    boolean set(UUID uuid, SettingFeature feature, boolean enabled);

    default boolean set(Player player, SettingFeature feature, boolean enabled) {
        return set(player.getUniqueId(), feature, enabled);
    }

    boolean toggle(UUID uuid, SettingFeature feature);

    default boolean toggle(Player player, SettingFeature feature) {
        return toggle(player.getUniqueId(), feature);
    }

    void apply(Player player, SettingFeature feature);

    void applyAll(Player player);

    static MarisSettingsApi get() {
        return MarisSettingsPlugin.getApi();
    }
}