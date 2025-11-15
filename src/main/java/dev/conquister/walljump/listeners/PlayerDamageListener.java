package dev.conquister.walljump.listeners;

import dev.conquister.walljump.WallJump;
import dev.conquister.walljump.player.PlayerManager;
import dev.conquister.walljump.player.WPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public record PlayerDamageListener(PlayerManager playerManager) implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        try {
            if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL) && event.getEntityType().equals(EntityType.PLAYER)) {
                WPlayer wplayer = playerManager.getWPlayer((Player) event.getEntity());
                if (wplayer != null && wplayer.isSliding()) {
                    event.setCancelled(true);
                    wplayer.onWallJumpEnd(false);
                }
            }
        } catch (Exception e) {
            WallJump.warning("An error occurred while handling a player damage event.");
        }
    }
}