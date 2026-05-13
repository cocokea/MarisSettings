package com.maris7.settings.service;

import com.maris7.settings.MarisSettingsPlugin;
import com.maris7.settings.model.GuiSettingItem;
import com.maris7.settings.model.SettingFeature;
import com.maris7.settings.storage.DatabaseManager;
import com.maris7.settings.storage.PlayerSettingsRepository;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class PlayerSettingsService {
    private final MarisSettingsPlugin plugin;
    private final PlayerSettingsRepository repository;
    private final Map<UUID, EnumMap<SettingFeature, Boolean>> cache = new ConcurrentHashMap<>();
    private final Map<SettingFeature, Boolean> defaults = new EnumMap<>(SettingFeature.class);
    private final List<GuiSettingItem> guiItems = new ArrayList<>();
    private final Map<UUID, Long> clickCooldowns = new ConcurrentHashMap<>();
    private final Set<UUID> dirty = ConcurrentHashMap.newKeySet();

    public PlayerSettingsService(MarisSettingsPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.repository = new PlayerSettingsRepository(databaseManager);
        reloadDefaults();
    }

    public void reloadDefaults() {
        defaults.clear();
        guiItems.clear();
        var section = plugin.getConfig().getConfigurationSection("settings-gui.items");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            var itemSection = section.getConfigurationSection(key);
            if (itemSection == null) {
                continue;
            }
            SettingFeature feature;
            try {
                feature = SettingFeature.fromConfig(itemSection.getString("feature", key));
            } catch (Exception ignored) {
                continue;
            }
            defaults.put(feature, itemSection.getBoolean("default", true));
            Material material = Material.matchMaterial(itemSection.getString("material", "PAPER"));
            if (material == null) {
                material = Material.PAPER;
            }
            guiItems.add(new GuiSettingItem(
                    key,
                    itemSection.getInt("slot", 0),
                    material,
                    itemSection.getString("display-name", key),
                    itemSection.getStringList("lore").isEmpty()
                            ? itemSection.getString("lore", "&fTrạng thái: %status%")
                            : String.join("\n", itemSection.getStringList("lore")),
                    feature,
                    itemSection.getString("sound", "ENTITY_EXPERIENCE_ORB_PICKUP"),
                    itemSection.getString("permission", ""),
                    itemSection.getBoolean("default", true),
                    itemSection.getString("command", "")
            ));
        }
        defaults.putIfAbsent(SettingFeature.DEATH_MESSAGE, false);
    }

    public List<GuiSettingItem> guiItems() {
        return List.copyOf(guiItems);
    }

    public boolean isEnabled(UUID uuid, SettingFeature feature) {
        return load(uuid).getOrDefault(feature, defaults.getOrDefault(feature, true));
    }

    public boolean set(UUID uuid, SettingFeature feature, boolean enabled) {
        load(uuid).put(feature, enabled);
        markDirty(uuid);
        return enabled;
    }

    public boolean toggle(UUID uuid, SettingFeature feature) {
        boolean next = !isEnabled(uuid, feature);
        set(uuid, feature, next);
        return next;
    }

    public EnumMap<SettingFeature, Boolean> load(UUID uuid) {
        return cache.computeIfAbsent(uuid, ignored -> defaultMap());
    }

    public void loadAsync(UUID uuid) {
        load(uuid);
        runAsync(() -> {
            Map<SettingFeature, Boolean> stored = repository.load(uuid);
            cache.compute(uuid, (ignored, current) -> {
                EnumMap<SettingFeature, Boolean> map = current == null ? defaultMap() : new EnumMap<>(current);
                stored.forEach(map::put);
                return map;
            });
        });
    }

    public void markDirty(UUID uuid) {
        dirty.add(uuid);
    }

    public void flush(UUID uuid) {
        EnumMap<SettingFeature, Boolean> map = cache.get(uuid);
        if (map != null) {
            repository.save(uuid, new EnumMap<>(map));
            dirty.remove(uuid);
        }
    }

    public void flushAsync(UUID uuid) {
        EnumMap<SettingFeature, Boolean> map = cache.get(uuid);
        if (map == null) {
            dirty.remove(uuid);
            return;
        }
        EnumMap<SettingFeature, Boolean> snapshot = new EnumMap<>(map);
        runAsync(() -> {
            repository.save(uuid, snapshot);
            dirty.remove(uuid);
        });
    }

    public void flushDirtyNow() {
        for (UUID uuid : new HashSet<>(dirty)) {
            flush(uuid);
        }
    }

    public void flushAll() {
        for (UUID uuid : cache.keySet()) {
            flush(uuid);
        }
    }

    public void forget(UUID uuid) {
        EnumMap<SettingFeature, Boolean> map = cache.remove(uuid);
        clickCooldowns.remove(uuid);
        if (map == null) {
            dirty.remove(uuid);
            return;
        }
        EnumMap<SettingFeature, Boolean> snapshot = new EnumMap<>(map);
        runAsync(() -> {
            repository.save(uuid, snapshot);
            dirty.remove(uuid);
        });
    }

    public boolean isOnGuiCooldown(UUID uuid) {
        if (!plugin.getConfig().getBoolean("settings-gui.cooldown.enabled", true)) {
            return false;
        }
        double seconds = plugin.getConfig().getDouble("settings-gui.cooldown.duration", 0.5D);
        long until = clickCooldowns.getOrDefault(uuid, 0L);
        long now = System.currentTimeMillis();
        if (until > now) {
            return true;
        }
        clickCooldowns.put(uuid, now + (long) (seconds * 1000L));
        return false;
    }

    private EnumMap<SettingFeature, Boolean> defaultMap() {
        EnumMap<SettingFeature, Boolean> map = new EnumMap<>(SettingFeature.class);
        defaults.forEach(map::putIfAbsent);
        return map;
    }

    private void runAsync(Runnable runnable) {
        if (plugin.isEnabled() && plugin.isFoliaServer()) {
            Bukkit.getAsyncScheduler().runNow(plugin, task -> runnable.run());
            return;
        }
        if (plugin.isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
            return;
        }
        runnable.run();
    }

    public String statusText(boolean enabled) {
        var section = plugin.getConfig().getConfigurationSection("settings-gui.status");
        String fallback = enabled ? "ON" : "OFF";
        if (section == null) {
            return fallback;
        }
        String direct = section.getString(enabled ? "on" : "off");
        if (direct != null && !direct.isBlank()) {
            return direct;
        }
        // YAML 1.1 may coerce unquoted keys like on/off into boolean true/false.
        Object legacy = section.get(enabled ? "true" : "false");
        if (legacy instanceof String legacyText && !legacyText.isBlank()) {
            return legacyText;
        }
        return fallback;
    }

    public String title() {
        return plugin.getConfig().getString("settings-gui.title", "Cài đặt");
    }

    public int guiSlots() {
        int slots = plugin.getConfig().getInt("settings-gui.gui-slots", 54);
        return Math.max(9, Math.min(54, slots));
    }
}
