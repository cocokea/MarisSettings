package com.maris7.settings.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;

public final class ConfigMergeUtil {
    private ConfigMergeUtil() {
    }

    public static void saveDefaultResourceIfMissing(JavaPlugin plugin, String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }

    public static void mergeMissingKeys(JavaPlugin plugin, String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveDefaultResourceIfMissing(plugin, resourcePath);
            return;
        }

        YamlConfiguration current = new YamlConfiguration();
        YamlConfiguration defaults = new YamlConfiguration();
        try {
            current.load(file);
        } catch (IOException | InvalidConfigurationException ex) {
            plugin.getLogger().log(Level.WARNING, "Không thể đọc " + resourcePath + ", bỏ qua merge.", ex);
            return;
        }

        try (InputStream input = plugin.getResource(resourcePath)) {
            if (input == null) {
                plugin.getLogger().warning("Không tìm thấy resource mặc định: " + resourcePath);
                return;
            }
            defaults.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException ex) {
            plugin.getLogger().log(Level.WARNING, "Không thể đọc resource mặc định " + resourcePath + ", bỏ qua merge.", ex);
            return;
        }

        current.setDefaults(defaults);
        current.options().copyDefaults(true);

        String before;
        try {
            before = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Không thể đọc nội dung cũ của " + resourcePath + ", vẫn sẽ thử merge.", ex);
            before = null;
        }

        String after = current.saveToString();
        if (before != null && normalize(before).equals(normalize(after))) {
            return;
        }

        try {
            current.save(file);
            plugin.getLogger().info("Đã tự động thêm key còn thiếu vào " + resourcePath + ".");
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Không thể lưu file sau khi merge: " + resourcePath, ex);
        }
    }

    private static String normalize(String value) {
        return value.replace("\r\n", "\n").trim();
    }
}
