package com.maris7.settings.compat;

import com.maris7.settings.MarisSettingsPlugin;
import com.maris7.settings.model.SettingFeature;
import com.maris7.settings.service.PlayerSettingsService;
import com.maris7.settings.util.ColorUtil;
import com.maris7.settings.util.CommandUnregisterUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;

public final class CompatibilityService {
    private static final String NO_TEAM_MESSAGE = "&cYou don't have a team to do this.";

    private final MarisSettingsPlugin plugin;
    private final PlayerSettingsService settingsService;

    public CompatibilityService(MarisSettingsPlugin plugin, PlayerSettingsService settingsService) {
        this.plugin = plugin;
        this.settingsService = settingsService;
    }

    public void bootstrap() {
        CommandUnregisterUtil.unregister(plugin,
                "msgtoggle", "chattoggle",
                "fastbuy", "fastsell", "ahtoggle",
                "tpatoggle", "tpaheretoggle", "tpaguitoggle",
                "toggleworth", "dueltoggle",
                "paytoggle", "payalerts",
                "teamtoggle");
    }

    public void applyAll(Player player) {
        for (SettingFeature feature : SettingFeature.values()) {
            apply(player, feature, settingsService.isEnabled(player.getUniqueId(), feature));
        }
    }

    public void apply(Player player, SettingFeature feature, boolean enabled) {
        if (feature == SettingFeature.PLAYER_VISIBILITY && plugin.gameplay() != null) {
            plugin.gameplay().refreshViewerVisibility(player);
            return;
        }
        if (feature == SettingFeature.WORTHT_TOGGLE) {
            refreshWorthDisplay(player);
        }
    }

    private void refreshWorthDisplay(Player player) {
        if (!player.isOnline()) {
            return;
        }
        player.getScheduler().run(plugin, task -> {
            if (player.isOnline()) {
                player.updateInventory();
            }
        }, null);
    }

    public Boolean toggleTeamChat(Player player) {
        Object teams = teamService();
        if (teams == null || !hasTeam(teams, player.getUniqueId())) {
            notifyNoTeam(player);
            return null;
        }

        boolean enabled = !settingsService.isEnabled(player.getUniqueId(), SettingFeature.TEAM_CHAT);
        settingsService.set(player.getUniqueId(), SettingFeature.TEAM_CHAT, enabled);
        apply(player, SettingFeature.TEAM_CHAT, enabled);
        return enabled;
    }

    private Object teamService() {
        try {
            Plugin marisTeam = Bukkit.getPluginManager().getPlugin("MarisTeam");
            if (marisTeam == null || !marisTeam.isEnabled()) {
                return null;
            }
            Method teamsMethod = marisTeam.getClass().getMethod("teams");
            return teamsMethod.invoke(marisTeam);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private boolean hasTeam(Object teams, UUID uuid) {
        try {
            Method teamOfMethod = teams.getClass().getMethod("teamOf", UUID.class);
            return teamOfMethod.invoke(teams, uuid) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void notifyNoTeam(Player player) {
        player.sendMessage(ColorUtil.color(NO_TEAM_MESSAGE));
        player.sendActionBar(ColorUtil.color(NO_TEAM_MESSAGE));
        try {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
        } catch (Throwable ignored) {
        }
    }
}