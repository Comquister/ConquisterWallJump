package dev.conquister.walljump.listeners;

import dev.conquister.walljump.player.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public record PlayerJoinListener(PlayerManager playerManager) implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerManager.registerPlayer(event.getPlayer());
    }
}