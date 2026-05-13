package com.maris7.settings.command;

import com.maris7.settings.MarisSettingsPlugin;
import com.maris7.settings.gui.SettingsMenu;
import com.maris7.settings.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public final class SettingsCommand implements CommandExecutor, TabCompleter {
    private final MarisSettingsPlugin plugin;
    private final SettingsMenu menu;

    public SettingsCommand(MarisSettingsPlugin plugin) {
        this.plugin = plugin;
        this.menu = new SettingsMenu(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-only", "Only player")));
            return true;
        }
        if (!player.hasPermission("marissettings.use")) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission", "No permission")));
            return true;
        }
        player.openInventory(menu.build(player));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
