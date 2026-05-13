package com.maris7.settings.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Map;

public final class CommandUnregisterUtil {
    private CommandUnregisterUtil() {}

    @SuppressWarnings("unchecked")
    public static void unregister(Plugin owner, String... names) {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            Map<String, org.bukkit.command.Command> knownCommands = (Map<String, org.bukkit.command.Command>) knownCommandsField.get(commandMap);
            for (String name : names) {
                org.bukkit.command.Command command = knownCommands.get(name);
                if (command instanceof PluginCommand pluginCommand) {
                    pluginCommand.unregister(commandMap);
                }
                knownCommands.remove(name);
                knownCommands.remove(owner.getName().toLowerCase() + ":" + name.toLowerCase());
            }
        } catch (Exception ignored) {
        }
    }
}
