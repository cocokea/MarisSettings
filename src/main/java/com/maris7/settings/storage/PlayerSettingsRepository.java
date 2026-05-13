package com.maris7.settings.storage;

import com.maris7.settings.model.SettingFeature;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerSettingsRepository {
    private final DatabaseManager databaseManager;

    public PlayerSettingsRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Map<SettingFeature, Boolean> load(UUID uuid) {
        Map<SettingFeature, Boolean> result = new EnumMap<>(SettingFeature.class);
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT feature, enabled FROM marissettings_player_settings WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    try {
                        result.put(SettingFeature.fromConfig(rs.getString("feature")), rs.getBoolean("enabled"));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (SQLException ignored) {
        }
        return result;
    }

    public void save(UUID uuid, Map<SettingFeature, Boolean> values) {
        try (Connection connection = databaseManager.getConnection()) {
            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM marissettings_player_settings WHERE uuid = ?")) {
                delete.setString(1, uuid.toString());
                delete.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO marissettings_player_settings (uuid, feature, enabled) VALUES (?, ?, ?)")) {
                for (Map.Entry<SettingFeature, Boolean> entry : values.entrySet()) {
                    insert.setString(1, uuid.toString());
                    insert.setString(2, entry.getKey().name());
                    insert.setBoolean(3, entry.getValue());
                    insert.addBatch();
                }
                insert.executeBatch();
            }
        } catch (SQLException ignored) {
        }
    }
}