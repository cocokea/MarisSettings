package com.maris7.settings.model;

import org.bukkit.Material;

public record GuiSettingItem(
        String key,
        int slot,
        Material material,
        String displayName,
        String lore,
        SettingFeature feature,
        String sound,
        String permission,
        boolean defaultState,
        String command
) {}
