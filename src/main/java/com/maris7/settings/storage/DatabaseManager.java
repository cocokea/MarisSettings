package com.maris7.settings.storage;

import com.maris7.settings.MarisSettingsPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {
    private final MarisSettingsPlugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(MarisSettingsPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        plugin.getDataFolder().mkdirs();
        HikariConfig config = new HikariConfig();
        String type = plugin.getConfig().getString("database.type", "SQLITE");
        if ("MYSQL".equalsIgnoreCase(type)) {
            String host = plugin.getConfig().getString("database.mysql.host", "127.0.0.1");
            int port = plugin.getConfig().getInt("database.mysql.port", 3306);
            String database = plugin.getConfig().getString("database.mysql.database", "marissettings");
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + plugin.getConfig().getBoolean("database.mysql.use-ssl", false));
            config.setUsername(plugin.getConfig().getString("database.mysql.username", "root"));
            config.setPassword(plugin.getConfig().getString("database.mysql.password", ""));
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(Math.max(2, plugin.getConfig().getInt("database.mysql.pool-size", 10)));
        } else {
            String filename = plugin.getConfig().getString("database.sqlite.filename", "playerdata.db");
            File dbFile = new File(plugin.getDataFolder(), filename);
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(Math.max(1, plugin.getConfig().getInt("database.sqlite.pool-size", 1)));
        }
        config.setPoolName("MarisSettings-Hikari");
        config.setConnectionTimeout(10000L);
        config.setValidationTimeout(5000L);
        this.dataSource = new HikariDataSource(config);
        createTables();
    }

    private void createTables() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS marissettings_player_settings (uuid VARCHAR(36) NOT NULL, feature VARCHAR(64) NOT NULL, enabled BOOLEAN NOT NULL, PRIMARY KEY (uuid, feature))");
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not create MarisSettings tables", exception);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}