package com.maris7.settings.command;

import com.maris7.settings.MarisSettingsPlugin;
import com.maris7.settings.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public final class SettingsAdminCommand implements CommandExecutor, TabCompleter {
    private final MarisSettingsPlugin plugin;

    public SettingsAdminCommand(MarisSettingsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("marissettings.admin")) {
                sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission", "No permission")));
                return true;
            }
            plugin.reloadPlugin();
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.settings-reloaded", "Reloaded")));
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("marissettings.admin")) {
            return List.of("reload");
        }
        return List.of();
    }
}
