package com.maris7.settings.gui;

import com.maris7.settings.MarisSettingsPlugin;
import com.maris7.settings.model.GuiSettingItem;
import com.maris7.settings.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public final class SettingsMenu {
    private final MarisSettingsPlugin plugin;

    public SettingsMenu(MarisSettingsPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory build(Player player) {
        Inventory inventory = Bukkit.createInventory(new SettingsMenuHolder(), plugin.settings().guiSlots(), ColorUtil.color(plugin.settings().title()));
        for (GuiSettingItem item : plugin.settings().guiItems()) {
            boolean enabled = plugin.settings().isEnabled(player.getUniqueId(), item.feature());
            ItemStack stack = new ItemStack(item.material());
            ItemMeta meta = stack.getItemMeta();
            meta.displayName(ColorUtil.color(item.displayName()));
            List<net.kyori.adventure.text.Component> lore = Arrays.stream(item.lore().replace("%status%", plugin.settings().statusText(enabled)).split("\\n"))
                    .map(ColorUtil::color)
                    .toList();
            meta.lore(lore);
            stack.setItemMeta(meta);
            if (item.slot() >= 0 && item.slot() < inventory.getSize()) {
                inventory.setItem(item.slot(), stack);
            }
        }
        return inventory;
    }

    public void play(Player player, String soundName) {
        try {
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 1F, 1F);
        } catch (Exception ignored) {
        }
    }
}