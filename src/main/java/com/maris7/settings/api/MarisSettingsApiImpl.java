package com.maris7.settings.api;

import com.maris7.settings.MarisSettingsPlugin;
import com.maris7.settings.model.SettingFeature;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class MarisSettingsApiImpl implements MarisSettingsApi {
    private final MarisSettingsPlugin plugin;

    public MarisSettingsApiImpl(MarisSettingsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled(UUID uuid, SettingFeature feature) {
        return plugin.settings().isEnabled(uuid, feature);
    }

    @Override
    public boolean isEnabled(UUID uuid, String feature, boolean defaultValue) {
        try {
            return isEnabled(uuid, SettingFeature.fromConfig(feature));
        } catch (RuntimeException ignored) {
            return defaultValue;
        }
    }

    @Override
    public boolean set(UUID uuid, String feature, boolean enabled) {
        try {
            return set(uuid, SettingFeature.fromConfig(feature), enabled);
        } catch (RuntimeException ignored) {
            return enabled;
        }
    }

    @Override
    public boolean set(UUID uuid, SettingFeature feature, boolean enabled) {
        boolean previous = plugin.settings().isEnabled(uuid, feature);
        boolean state = plugin.settings().set(uuid, feature, enabled);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            plugin.compat().apply(player, feature, state);
        }
        if (previous != state) {
            Bukkit.getPluginManager().callEvent(new PlayerSettingChangeEvent(uuid, feature, state));
        }
        return state;
    }

    @Override
    public boolean toggle(UUID uuid, SettingFeature feature) {
        boolean previous = plugin.settings().isEnabled(uuid, feature);
        boolean state = plugin.settings().toggle(uuid, feature);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            plugin.compat().apply(player, feature, state);
        }
        if (previous != state) {
            Bukkit.getPluginManager().callEvent(new PlayerSettingChangeEvent(uuid, feature, state));
        }
        return state;
    }

    @Override
    public void apply(Player player, SettingFeature feature) {
        plugin.compat().apply(player, feature, plugin.settings().isEnabled(player.getUniqueId(), feature));
    }

    @Override
    public void applyAll(Player player) {
        plugin.compat().applyAll(player);
    }
}