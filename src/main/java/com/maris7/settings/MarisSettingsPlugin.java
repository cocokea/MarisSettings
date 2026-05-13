package com.maris7.settings;

import com.maris7.settings.api.MarisSettingsApi;
import com.maris7.settings.api.MarisSettingsApiImpl;
import com.maris7.settings.command.SettingsAdminCommand;
import com.maris7.settings.command.SettingsCommand;
import com.maris7.settings.compat.CompatibilityService;
import com.maris7.settings.listener.GameplayListener;
import com.maris7.settings.listener.LegacyCommandBlockListener;
import com.maris7.settings.listener.SettingsGuiListener;
import com.maris7.settings.packet.PacketFeatureService;
import com.maris7.settings.service.PlayerSettingsService;
import com.maris7.settings.storage.DatabaseManager;
import com.maris7.settings.task.AutoSaveTask;
import com.maris7.settings.util.ConfigMergeUtil;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public final class MarisSettingsPlugin extends JavaPlugin {
    private static MarisSettingsApi api;

    private DatabaseManager databaseManager;
    private PlayerSettingsService playerSettingsService;
    private CompatibilityService compatibilityService;
    private PacketFeatureService packetFeatureService;
    private GameplayListener gameplayListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigMergeUtil.saveDefaultResourceIfMissing(this, "messages.yml");
        ConfigMergeUtil.mergeMissingKeys(this, "config.yml");
        ConfigMergeUtil.mergeMissingKeys(this, "messages.yml");

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.start();

        this.playerSettingsService = new PlayerSettingsService(this, databaseManager);
        registerConfiguredPermissions();
        this.compatibilityService = new CompatibilityService(this, playerSettingsService);
        this.compatibilityService.bootstrap();

        this.packetFeatureService = new PacketFeatureService(this);
        this.packetFeatureService.register();

        api = new MarisSettingsApiImpl(this);
        getServer().getServicesManager().register(MarisSettingsApi.class, api, this, ServicePriority.Normal);

        SettingsCommand settingsCommand = new SettingsCommand(this);
        if (getCommand("settings") != null) {
            getCommand("settings").setExecutor(settingsCommand);
            getCommand("settings").setTabCompleter(settingsCommand);
        }
        SettingsAdminCommand settingsAdminCommand = new SettingsAdminCommand(this);
        if (getCommand("settingsadmin") != null) {
            getCommand("settingsadmin").setExecutor(settingsAdminCommand);
            getCommand("settingsadmin").setTabCompleter(settingsAdminCommand);
        }

        this.gameplayListener = new GameplayListener(this);
        Bukkit.getPluginManager().registerEvents(new SettingsGuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(gameplayListener, this);
        Bukkit.getPluginManager().registerEvents(new LegacyCommandBlockListener(this), this);

        long autosaveSeconds = Math.max(15L, getConfig().getLong("database.autosave-seconds", 120L));
        if (isFoliaServer()) {
            Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> new AutoSaveTask(this).run(), autosaveSeconds, autosaveSeconds, TimeUnit.SECONDS);
            return;
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> new AutoSaveTask(this).run(), autosaveSeconds * 20L, autosaveSeconds * 20L);
    }

    @Override
    public void onDisable() {
        Bukkit.getServicesManager().unregisterAll(this);
        api = null;
        if (playerSettingsService != null) {
            playerSettingsService.flushAll();
        }
        if (packetFeatureService != null) {
            packetFeatureService.unregister();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        playerSettingsService.reloadDefaults();
        registerConfiguredPermissions();
        compatibilityService.bootstrap();
    }

    private void registerConfiguredPermissions() {
        for (var item : playerSettingsService.guiItems()) {
            String permission = item.permission();
            if (permission == null || permission.isBlank()) {
                continue;
            }
            if (getServer().getPluginManager().getPermission(permission) != null) {
                continue;
            }
            try {
                getServer().getPluginManager().addPermission(new Permission(permission, PermissionDefault.FALSE));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static MarisSettingsApi getApi() {
        return api;
    }

    public DatabaseManager databaseManager() { return databaseManager; }
    public PlayerSettingsService settings() { return playerSettingsService; }
    public CompatibilityService compat() { return compatibilityService; }
    public PacketFeatureService packetFeatures() { return packetFeatureService; }
    public GameplayListener gameplay() { return gameplayListener; }

    public boolean isFoliaServer() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
