package dev.thomazz.pledge.inject;

import dev.thomazz.pledge.PledgeImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

// We only need this to inject players on login
public class InjectListener implements Listener {
    @EventHandler
    public void onSpawnEvent(PlayerLoginEvent event) {
        PledgeImpl.INSTANCE.getTransactionManager().createTransactionHandler(event.getPlayer());
    }
}
