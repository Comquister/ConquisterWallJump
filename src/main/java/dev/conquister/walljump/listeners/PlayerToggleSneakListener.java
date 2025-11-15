package dev.conquister.walljump.listeners;

import dev.conquister.walljump.WallJump;
import dev.conquister.walljump.player.PlayerManager;
import dev.conquister.walljump.player.WPlayer;
import dev.conquister.walljump.utils.LocationUtils;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public record PlayerToggleSneakListener(PlayerManager playerManager) implements Listener {

    public PlayerToggleSneakListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
        WallJump.debug("PlayerToggleSneakListener initialized with PlayerManager: " + (playerManager != null));
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleSneak(@NotNull PlayerToggleSneakEvent event) {
        try {
            Player player = event.getPlayer();
            WallJump.debug("PlayerToggleSneak event for: " + player.getName());

            if (!player.isFlying()) {
                WallJump.debug("Player is not flying, checking wall status...");

                WPlayer wplayer = playerManager.getWPlayer(player);
                if (wplayer == null) {
                    WallJump.warning("[WallJump] WPlayer is null for " + player.getName());
                    return;
                }

                WallJump.debug("WPlayer found. IsOnWall: " + wplayer.isOnWall() + ", IsSneaking: " + event.isSneaking());

                if (wplayer.isOnWall() && !event.isSneaking()) {
                    WallJump.debug("Ending wall jump for " + player.getName());
                    wplayer.onWallJumpEnd();
                } else if (LocationUtils.isTouchingAWall(player) && event.isSneaking() && !player.isOnGround()) {
                    WallJump.debug("Starting wall jump for " + player.getName());
                    wplayer.onWallJumpStart();
                }
            } else {
                WallJump.debug("Player is flying, ignoring event");
            }
        } catch (Exception e) {
            WallJump.severe("[WallJump] ERROR in PlayerToggleSneakEvent:");
            WallJump.severe("[WallJump] Error message: " + e.getMessage());
            WallJump.severe("[WallJump] Error class: " + e.getClass().getName());
            e.printStackTrace();
        }
    }
}