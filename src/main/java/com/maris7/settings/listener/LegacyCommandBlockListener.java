package com.maris7.settings.listener;

import com.maris7.settings.MarisSettingsPlugin;
import com.maris7.settings.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;
import java.util.Set;

public final class LegacyCommandBlockListener implements Listener {
    private static final Set<String> BLOCKED = Set.of(
            "msgtoggle", "chattoggle",
            "fastbuy", "fastsell", "ahtoggle",
            "tpatoggle", "tpaheretoggle", "tpaguitoggle",
            "toggleworth", "dueltoggle",
            "paytoggle", "payalerts"
    );

    private final MarisSettingsPlugin plugin;

    public LegacyCommandBlockListener(MarisSettingsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message == null || message.length() < 2 || message.charAt(0) != '/') {
            return;
        }
        String label = message.substring(1).split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        if (!BLOCKED.contains(label)) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        player.sendMessage(ColorUtil.color(plugin.getConfig().getString(
                "messages.legacy-command-redirect",
                "&#FFD166This toggle moved to &#FFFFFF/settings"
        )));
    }
}