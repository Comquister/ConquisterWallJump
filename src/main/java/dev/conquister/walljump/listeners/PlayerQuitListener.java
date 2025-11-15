package dev.conquister.walljump.listeners;

import dev.conquister.walljump.player.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public record PlayerQuitListener(PlayerManager playerManager) implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerManager.unregisterPlayer(event.getPlayer());
    }
}