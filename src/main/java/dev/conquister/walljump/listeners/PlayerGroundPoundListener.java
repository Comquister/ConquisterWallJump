package dev.conquister.walljump.listeners;

import dev.conquister.walljump.WallJump;
import dev.conquister.walljump.player.PlayerManager;
import dev.conquister.walljump.utils.LocationUtils;
import dev.conquister.walljump.api.events.GroundPoundEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public record PlayerGroundPoundListener(PlayerManager playerManager) implements Listener {

    @EventHandler
    public void onGroundPound(GroundPoundEvent event) {
        try {
            if (playerManager.getWPlayer(event.getPlayer()).isWallJumping() || LocationUtils.isTouchingAWall(event.getPlayer()))
                event.setCancelled(true);
        } catch (Exception e) {
            WallJump.warning("An error occurred while handling the GroundPoundEvent");
        }
    }
}