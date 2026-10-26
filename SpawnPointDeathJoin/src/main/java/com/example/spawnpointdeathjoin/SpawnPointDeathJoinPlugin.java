package com.example.spawnpointdeathjoin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpawnPointDeathJoinPlugin extends JavaPlugin implements Listener, CommandExecutor {
    private static final String SPAWN_WORLD = "spawn.world";
    @Override public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("spawnpointdeathjoin").setExecutor(this);
    }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player in-game.");
            return true;
        }
        Location location = player.getLocation();
        getConfig().set(SPAWN_WORLD, location.getWorld().getName());
        getConfig().set("spawn.x", location.getX()); getConfig().set("spawn.y", location.getY());
        getConfig().set("spawn.z", location.getZ()); getConfig().set("spawn.yaw", location.getYaw());
        getConfig().set("spawn.pitch", location.getPitch()); saveConfig();
        player.sendMessage(formatMessage("messages.spawn-set", player.getName()));
        return true;
    }
    @EventHandler public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(formatMessage("messages.welcome", player.getName()));
        Location spawn = getConfiguredSpawn();
        if (spawn == null) player.sendMessage(formatMessage("messages.spawn-unset", player.getName()));
        else player.teleport(spawn);
    }
    @EventHandler public void onPlayerRespawn(PlayerRespawnEvent event) {
        Location spawn = getConfiguredSpawn();
        if (spawn != null) event.setRespawnLocation(spawn);
    }
    @EventHandler public void onPlayerQuit(PlayerQuitEvent event) {
        event.quitMessage(formatMessage("messages.farewell", event.getPlayer().getName()));
    }
    private Location getConfiguredSpawn() {
        String worldName = getConfig().getString(SPAWN_WORLD, "");
        if (worldName == null || worldName.isBlank()) return null;
        World world = getServer().getWorld(worldName);
        if (world == null) return null;
        return new Location(world, getConfig().getDouble("spawn.x"), getConfig().getDouble("spawn.y"),
                getConfig().getDouble("spawn.z"), (float) getConfig().getDouble("spawn.yaw"),
                (float) getConfig().getDouble("spawn.pitch"));
    }
    private String formatMessage(String path, String playerName) {
        String message = getConfig().getString(path, "");
        if (message == null || message.isEmpty()) return "";
        return ChatColor.translateAlternateColorCodes('&', message.replace("{player}", playerName));
    }
}
