package com.maris7.settings.listener;

import com.maris7.settings.MarisSettingsPlugin;
import com.maris7.settings.gui.SettingsMenu;
import com.maris7.settings.gui.SettingsMenuHolder;
import com.maris7.settings.api.PlayerSettingChangeEvent;
import com.maris7.settings.model.GuiSettingItem;
import com.maris7.settings.model.SettingFeature;
import com.maris7.settings.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public final class SettingsGuiListener implements Listener {
    private final MarisSettingsPlugin plugin;
    private final SettingsMenu menu;

    public SettingsGuiListener(MarisSettingsPlugin plugin) {
        this.plugin = plugin;
        this.menu = new SettingsMenu(plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof SettingsMenuHolder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getRawSlot() < 0 || event.getRawSlot() >= top.getSize()) {
            return;
        }
        if (plugin.settings().isOnGuiCooldown(player.getUniqueId())) {
            return;
        }

        for (GuiSettingItem item : plugin.settings().guiItems()) {
            if (item.slot() != event.getRawSlot()) {
                continue;
            }

            if (!item.permission().isBlank() && !player.hasPermission(item.permission())) {
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission", "&cNo permission")));
                return;
            }

            if (item.feature() == SettingFeature.TEAM_CHAT) {
                Boolean enabled = plugin.compat().toggleTeamChat(player);
                if (enabled != null) {
                    menu.play(player, item.sound());
                }
                player.openInventory(menu.build(player));
                return;
            }

            boolean previous = plugin.settings().isEnabled(player.getUniqueId(), item.feature());
            boolean enabled = plugin.settings().toggle(player.getUniqueId(), item.feature());
            plugin.compat().apply(player, item.feature(), enabled);
            if (previous != enabled) {
                Bukkit.getPluginManager().callEvent(new PlayerSettingChangeEvent(player.getUniqueId(), item.feature(), enabled));
            }
            menu.play(player, item.sound());
            if (item.feature() == SettingFeature.COMMAND && !item.command().isBlank()) {
                Bukkit.dispatchCommand(player, item.command());
            }
            player.openInventory(menu.build(player));
            return;
        }
    }
}