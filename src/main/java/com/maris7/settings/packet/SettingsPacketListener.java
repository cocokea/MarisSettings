package com.maris7.settings.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.maris7.settings.MarisSettingsPlugin;
import com.maris7.settings.model.SettingFeature;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class SettingsPacketListener extends PacketListenerAbstract {
    private static final int TOTEM_STATUS = 35;

    private final MarisSettingsPlugin plugin;

    public SettingsPacketListener(MarisSettingsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        Object rawPlayer = event.getPlayer();
        if (!(rawPlayer instanceof Player player)) {
            return;
        }

        try {
            if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {
                WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
                if (wrapper.isOverlay()) {
                    if (!plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.HOTBAR_SERVER_MESSAGES)) {
                        event.setCancelled(true);
                        return;
                    }
                } else if (!plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.CHAT_SERVER_MESSAGE)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }

        String packetName = event.getPacketType() == null ? "" : event.getPacketType().toString().toUpperCase(Locale.ROOT);

        try {
            if (packetName.contains("ACTION_BAR")
                    && !plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.HOTBAR_SERVER_MESSAGES)) {
                event.setCancelled(true);
                return;
            }

            if (isChatMessagePacket(packetName)
                    && !plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.CHAT_SERVER_MESSAGE)) {
                event.setCancelled(true);
                return;
            }

            if (packetName.contains("ENTITY_STATUS")) {
                WrapperPlayServerEntityStatus wrapper = new WrapperPlayServerEntityStatus(event);
                if (wrapper.getStatus() == TOTEM_STATUS
                        && !plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.TOTEM_PARTICLES)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (packetName.contains("EXPLOSION")) {
                WrapperPlayServerExplosion wrapper = new WrapperPlayServerExplosion(event);
                if (!plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.EXPLOSION_PARTICLES)) {
                    Particle<?> safeParticle = safeExplosionReplacementParticle();
                    try {
                        wrapper.setParticle(safeParticle);
                    } catch (Throwable ignored) {
                    }
                    try {
                        wrapper.setSmallExplosionParticles(safeParticle);
                        wrapper.setLargeExplosionParticles(safeParticle);
                    } catch (Throwable ignored) {
                    }
                    wrapper.write();
                    event.markForReEncode(true);
                }

                if (!plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.EXPLOSION_SOUNDS)) {
                    try {
                        wrapper.setExplosionSound(Sounds.INTENTIONALLY_EMPTY);
                        wrapper.write();
                        event.markForReEncode(true);
                    } catch (Throwable ignored) {
                    }
                }
                return;
            }

            if (packetName.contains("PARTICLE")) {
                WrapperPlayServerParticle wrapper = new WrapperPlayServerParticle(event);
                Particle<?> particle = null;
                try {
                    particle = wrapper.getParticle();
                } catch (Throwable ignored) {
                }
                if (particle != null
                        && isExplosionParticle(particle)
                        && !plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.EXPLOSION_PARTICLES)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (packetName.contains("ENTITY_SOUND_EFFECT")) {
                if (!plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.SOUND_NOTIFICATIONS)) {
                    event.setCancelled(true);
                    return;
                }
                WrapperPlayServerEntitySoundEffect wrapper = new WrapperPlayServerEntitySoundEffect(event);
                Sound sound = wrapper.getSound();
                if (isTotemSound(sound)
                        && !plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.TOTEM_PARTICLES)) {
                    event.setCancelled(true);
                    return;
                }
                if (isExplosionSound(sound)
                        && !plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.EXPLOSION_SOUNDS)) {
                    event.setCancelled(true);
                }
                return;
            }

            if (packetName.contains("SOUND_EFFECT") || packetName.contains("NAMED_SOUND_EFFECT")) {
                if (!plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.SOUND_NOTIFICATIONS)) {
                    event.setCancelled(true);
                    return;
                }
                WrapperPlayServerSoundEffect wrapper = new WrapperPlayServerSoundEffect(event);
                Sound sound = wrapper.getSound();
                if (isTotemSound(sound)
                        && !plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.TOTEM_PARTICLES)) {
                    event.setCancelled(true);
                    return;
                }
                if (isExplosionSound(sound)
                        && !plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.EXPLOSION_SOUNDS)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (packetName.contains("STOP_SOUND")
                    && !plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.SOUND_NOTIFICATIONS)) {
                event.setCancelled(true);
            }
        } catch (Throwable ignored) {
            // Fail open to avoid breaking packets on PacketEvents minor API differences.
        }
    }

    private boolean isChatMessagePacket(String packetName) {
        if (packetName.contains("ACTION_BAR")) {
            return false;
        }
        return packetName.contains("CHAT") || packetName.contains("PLAYER_MESSAGE") || packetName.contains("DISGUISED");
    }

    private Particle<?> safeExplosionReplacementParticle() {
        ParticleDustData dustData = new ParticleDustData(0f, 0f, 0f, 0f);
        return new Particle<>(ParticleTypes.DUST, dustData);
    }

    private boolean isExplosionParticle(Particle<?> particle) {
        if (particle == null || particle.getType() == null) {
            return false;
        }
        String normalized = safeParticleName(particle).toLowerCase(Locale.ROOT);
        return normalized.contains("explosion") || normalized.contains("poof");
    }

    private boolean isTotemSound(Sound sound) {
        return safeSoundName(sound).contains("totem");
    }

    private boolean isExplosionSound(Sound sound) {
        String normalized = safeSoundName(sound);
        return normalized.contains("explode") || normalized.contains("explosion");
    }

    private String safeParticleName(Particle<?> particle) {
        try {
            Object name = particle.getType().getName();
            if (name != null) {
                return name.toString();
            }
        } catch (Throwable ignored) {
        }
        try {
            Object type = particle.getType();
            if (type != null) {
                return type.toString();
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    private String safeSoundName(Sound sound) {
        if (sound == null) {
            return "";
        }
        try {
            if (sound.getSoundId() != null) {
                return sound.getSoundId().toString().toLowerCase(Locale.ROOT);
            }
        } catch (Throwable ignored) {
        }
        try {
            Object name = sound.getName();
            if (name != null) {
                return name.toString().toLowerCase(Locale.ROOT);
            }
        } catch (Throwable ignored) {
        }
        return String.valueOf(sound).toLowerCase(Locale.ROOT);
    }
}
