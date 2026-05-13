package com.maris7.settings.listener;

import com.maris7.settings.MarisSettingsPlugin;
import com.maris7.settings.model.SettingFeature;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class GameplayListener implements Listener {
    private static final double MOB_SPAWN_RADIUS_SQUARED = 64D * 64D;

    private final MarisSettingsPlugin plugin;

    public GameplayListener(MarisSettingsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.settings().loadAsync(player.getUniqueId());
        if (plugin.isFoliaServer()) {
            Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
                refreshViewerVisibility(player);
                refreshVisibilityForNewTarget(player);
            });
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            refreshViewerVisibility(player);
            refreshVisibilityForNewTarget(player);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.settings().forget(event.getPlayer().getUniqueId());
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showEntity(plugin, event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!isNaturalMonsterSpawn(event)) {
            return;
        }

        Entity entity = event.getEntity();
        for (Player player : entity.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(entity.getLocation()) > MOB_SPAWN_RADIUS_SQUARED) {
                continue;
            }
            if (plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.DISABLE_MOB_SPAWN)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean isNaturalMonsterSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Monster) || entity instanceof EnderDragon) {
            return false;
        }
        return switch (event.getSpawnReason()) {
            case NATURAL, CHUNK_GEN, DEFAULT, SPAWNER, LIGHTNING,
                    OCELOT_BABY, MOUNT, REINFORCEMENTS, PATROL, RAID,
                    TRAP, DROWNED, SLIME_SPLIT, PIGLIN_ZOMBIFIED,
                    INFECTION, SPELL, SHEARED, SILVERFISH_BLOCK, NETHER_PORTAL,
                    FROZEN -> true;
            default -> false;
        };
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTotemUse(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.TOTEM_PARTICLES)) {
            return;
        }
        if (player.getHealth() - event.getFinalDamage() > 0.0D) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean useMainHand = mainHand.getType() == Material.TOTEM_OF_UNDYING;
        boolean useOffHand = offHand.getType() == Material.TOTEM_OF_UNDYING;
        if (!useMainHand && !useOffHand) {
            return;
        }

        event.setCancelled(true);
        if (useMainHand) {
            player.getInventory().setItemInMainHand(consumeOne(mainHand));
        } else {
            player.getInventory().setItemInOffHand(consumeOne(offHand));
        }

        player.setHealth(1.0D);
        player.setFireTicks(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
        player.updateInventory();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!plugin.settings().isEnabled(player.getUniqueId(), SettingFeature.CHAINMAIL_SPAWN)) {
            return;
        }
        event.getItemsToKeep().add(new ItemStack(Material.CHAINMAIL_HELMET));
        event.getItemsToKeep().add(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        event.getItemsToKeep().add(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        event.getItemsToKeep().add(new ItemStack(Material.CHAINMAIL_BOOTS));
        event.getItemsToKeep().add(new ItemStack(Material.COOKED_BEEF, 16));
        event.getItemsToKeep().forEach(item -> {
            if (item == null) return;
            event.getDrops().removeIf(drop -> drop != null && drop.isSimilar(item));
        });
        player.getInventory().setItem(0, new ItemStack(Material.CHAINMAIL_HELMET));
        player.getInventory().setItem(1, new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        player.getInventory().setItem(2, new ItemStack(Material.CHAINMAIL_LEGGINGS));
        player.getInventory().setItem(3, new ItemStack(Material.CHAINMAIL_BOOTS));
        player.getInventory().setItem(4, new ItemStack(Material.COOKED_BEEF, 16));
    }

    public void refreshViewerVisibility(Player viewer) {
        boolean visible = plugin.settings().isEnabled(viewer.getUniqueId(), SettingFeature.PLAYER_VISIBILITY);
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) {
                continue;
            }
            if (visible) {
                viewer.showEntity(plugin, target);
            } else {
                viewer.hideEntity(plugin, target);
            }
        }
    }

    public void refreshVisibilityForNewTarget(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) {
                continue;
            }
            boolean visible = plugin.settings().isEnabled(viewer.getUniqueId(), SettingFeature.PLAYER_VISIBILITY);
            if (visible) {
                viewer.showEntity(plugin, target);
            } else {
                viewer.hideEntity(plugin, target);
            }
        }
    }

    private ItemStack consumeOne(ItemStack item) {
        ItemStack clone = item.clone();
        clone.setAmount(Math.max(0, clone.getAmount() - 1));
        return clone;
    }
}
