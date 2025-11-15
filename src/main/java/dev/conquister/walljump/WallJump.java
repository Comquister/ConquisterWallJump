package dev.conquister.walljump;

import dev.conquister.walljump.api.WallJumpAPI;
import dev.conquister.walljump.command.WallJumpCommand;
import dev.conquister.walljump.config.WallJumpConfiguration;
import dev.conquister.walljump.listeners.*;
import dev.conquister.walljump.handlers.WorldGuardHandler;
import dev.conquister.walljump.player.PlayerManager;
import dev.conquister.walljump.player.WPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;
import java.util.logging.Level;
import com.tcoded.folialib.FoliaLib;

public final class WallJump extends JavaPlugin {
    private FoliaLib foliaLib;
    private static WallJump plugin;
    private PlayerManager playerManager;
    private WallJumpConfiguration config;
    private WallJumpConfiguration dataConfig;
    private WorldGuardHandler worldGuard;
    private static boolean debugMode = false;

    public WallJumpAPI api;

    public static WallJump getInstance() {
        return plugin;
    }

    public FoliaLib getFoliaLib() {
        return foliaLib;
    }

    public WallJumpAPI getAPI() {
        return api;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public WallJumpConfiguration getWallJumpConfig() {
        return config;
    }

    public WallJumpConfiguration getDataConfig() {
        return dataConfig;
    }

    public WorldGuardHandler getWorldGuardHandler() {
        return worldGuard;
    }

    // Métodos de debug
    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
    }

    public static void debug(String message) {
        if (debugMode) {
            getInstance().getLogger().info("[DEBUG] " + message);
        }
    }

    // Métodos de logging (para evitar usar Bukkit.getLogger())
    public static void log(String message) {
        getInstance().getLogger().info(message);
    }

    public static void warning(String message) {
        getInstance().getLogger().warning(message);
    }

    public static void severe(String message) {
        getInstance().getLogger().severe(message);
    }

    @Override
    public void onEnable() {
        try {
            foliaLib = new FoliaLib(this);
            playerManager = new PlayerManager();
            log("PlayerManager initialized.");

            registerEvents(
                    new PlayerJoinListener(playerManager),
                    new PlayerQuitListener(playerManager),
                    new PlayerToggleSneakListener(playerManager),
                    new PlayerDamageListener(playerManager),
                    new PlayerGroundPoundListener(playerManager)
            );
            log("Listeners registered.");

            Objects.requireNonNull(this.getCommand("walljump")).setExecutor(new WallJumpCommand());

            for (Player player : Bukkit.getOnlinePlayers()) {
                playerManager.registerPlayer(player);
            }

            api = new WallJumpAPI();
            log("WallJump has been enabled!");

        } catch (Exception e) {
            severe("WallJump has failed to enable!");
            severe("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onLoad() {
        plugin = this;
        config = new WallJumpConfiguration("config.yml");
        dataConfig = new WallJumpConfiguration("data.yml");

        debugMode = config.getBoolean("debug", false);
        if (debugMode) {
            log("Debug mode ENABLED");
        }

        Plugin worldGuardPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin != null) {
            worldGuard = new WorldGuardHandler(worldGuardPlugin);
        }
    }

    @Override
    public void onDisable() {
        if (config.getBoolean("toggleCommand")) {
            for (WPlayer wplayer : playerManager.getWPlayers()) {
                dataConfig.set(wplayer.getPlayer().getUniqueId().toString(), wplayer.enabled);
            }
            dataConfig.save();
        }
        log("WallJump has been disabled!");
    }

    private void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }
}