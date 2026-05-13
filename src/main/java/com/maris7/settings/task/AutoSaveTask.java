package com.maris7.settings.task;

import com.maris7.settings.MarisSettingsPlugin;

public final class AutoSaveTask implements Runnable {
    private final MarisSettingsPlugin plugin;

    public AutoSaveTask(MarisSettingsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.settings().flushDirtyNow();
    }
}
