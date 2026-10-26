package com.example.spawnpointdeathjoin;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpawnPointDeathJoinPlugin extends JavaPlugin implements Listener, CommandExecutor {
    private static final String SPAWN_WORLD = "spawn.world";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("spawnpointdeathjoin") != null) {
            getCommand("spawnpointdeathjoin").setExecutor(this);
        }

        long groundCleanupTicks = secondsToTicks("cleanup.ground-items-interval-seconds", 300);
        getServer().getScheduler().runTaskTimer(this, this::clearGroundItems, groundCleanupTicks, groundCleanupTicks);

        long illegalCheckTicks = secondsToTicks("cleanup.illegal-item-check-interval-seconds", 5);
        getServer().getScheduler().runTaskTimer(this, this::removeIllegalItemsFromPlayers, illegalCheckTicks, illegalCheckTicks);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player in-game.");
            return true;
        }

        Location location = player.getLocation();
        getConfig().set(SPAWN_WORLD, location.getWorld().getName());
        getConfig().set("spawn.x", location.getX());
        getConfig().set("spawn.y", location.getY());
        getConfig().set("spawn.z", location.getZ());
        getConfig().set("spawn.yaw", location.getYaw());
        getConfig().set("spawn.pitch", location.getPitch());
        saveConfig();
        player.sendMessage(formatMessage("messages.spawn-set", player.getName()));
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(formatMessage("messages.welcome", player.getName()));
        Location spawn = getConfiguredSpawn();
        if (spawn == null) {
            player.sendMessage(formatMessage("messages.spawn-unset", player.getName()));
            return;
        }
        player.teleport(spawn);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Location spawn = getConfiguredSpawn();
        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.quitMessage(LegacyComponentSerializer.legacySection().deserialize(
                formatMessage("messages.farewell", event.getPlayer().getName())));
    }

    private void clearGroundItems() {
        int removed = 0;
        for (World world : getServer().getWorlds()) {
            for (Item item : world.getEntitiesByClass(Item.class)) {
                item.remove();
                removed++;
            }
        }
        if (removed > 0) {
            Bukkit.broadcastMessage(formatMessage("messages.ground-items-cleared", "")
                    .replace("{count}", Integer.toString(removed)));
        }
    }

    private void removeIllegalItemsFromPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                continue;
            }

            int removed = removeIllegalItems(player.getInventory());
            removed += removeIllegalItems(player.getEnderChest());
            if (removed > 0) {
                player.sendMessage(formatMessage("messages.illegal-items-removed", player.getName()));
            }
        }
    }

    private int removeIllegalItems(Inventory inventory) {
        int removed = 0;
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack stack = contents[slot];
            if (stack != null && isIllegalItem(stack.getType())) {
                removed += stack.getAmount();
                inventory.setItem(slot, null);
            }
        }
        return removed;
    }

    private boolean isIllegalItem(Material material) {
        return material == Material.SPAWNER || material.name().endsWith("_SPAWN_EGG");
    }

    private long secondsToTicks(String configPath, long defaultSeconds) {
        long seconds = Math.max(1L, getConfig().getLong(configPath, defaultSeconds));
        return seconds * 20L;
    }

    private Location getConfiguredSpawn() {
        String worldName = getConfig().getString(SPAWN_WORLD, "");
        if (worldName == null || worldName.isBlank()) {
            return null;
        }
        World world = getServer().getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world,
                getConfig().getDouble("spawn.x"),
                getConfig().getDouble("spawn.y"),
                getConfig().getDouble("spawn.z"),
                (float) getConfig().getDouble("spawn.yaw"),
                (float) getConfig().getDouble("spawn.pitch"));
    }

    private String formatMessage(String path, String playerName) {
        String message = getConfig().getString(path, "");
        if (message == null || message.isEmpty()) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message.replace("{player}", playerName));
    }
}
