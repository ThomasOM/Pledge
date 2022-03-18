package dev.thomazz.pledge.inject;

import dev.thomazz.pledge.PledgeImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

// We only need this to inject players on join
public class InjectListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PledgeImpl.INSTANCE.getTransactionManager().createTransactionHandler(event.getPlayer());
    }
}
