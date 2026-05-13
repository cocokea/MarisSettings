package com.maris7.settings.api;

import com.maris7.settings.model.SettingFeature;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class PlayerSettingChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID playerId;
    private final SettingFeature feature;
    private final boolean enabled;

    public PlayerSettingChangeEvent(@NotNull UUID playerId, @NotNull SettingFeature feature, boolean enabled) {
        this.playerId = playerId;
        this.feature = feature;
        this.enabled = enabled;
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public @NotNull SettingFeature getFeature() {
        return feature;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
