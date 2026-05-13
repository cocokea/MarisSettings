package com.maris7.settings.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.maris7.settings.MarisSettingsPlugin;

public final class PacketFeatureService {
    private final MarisSettingsPlugin plugin;
    private final SettingsPacketListener listener;
    private boolean registered;

    public PacketFeatureService(MarisSettingsPlugin plugin) {
        this.plugin = plugin;
        this.listener = new SettingsPacketListener(plugin);
    }

    public void register() {
        if (registered) {
            return;
        }
        PacketEvents.getAPI().getEventManager().registerListener(listener);
        registered = true;
    }

    public void unregister() {
        if (!registered) {
            return;
        }
        PacketEvents.getAPI().getEventManager().unregisterListener(listener);
        registered = false;
    }
}
