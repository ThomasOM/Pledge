package dev.thomazz.pledge.transaction;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.api.Direction;
import dev.thomazz.pledge.api.event.TransactionEvent;
import dev.thomazz.pledge.api.event.TransactionListener;
import dev.thomazz.pledge.inject.net.PledgePacketHandler;
import dev.thomazz.pledge.util.MinecraftUtil;
import dev.thomazz.pledge.util.PacketUtil;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TransactionManager {
    private final List<TransactionHandler> transactionHandlers = new ArrayList<>();
    private final List<TransactionListener> listeners = new CopyOnWriteArrayList<>();

    private Direction direction;
    private int min;
    private int max;

    public TransactionManager() {
        this.direction = Direction.NEGATIVE;
        this.max = -1;

        // Different default ranges depending on mode
        switch (PacketUtil.MODE) {
            case TRANSACTION:
                this.min = Short.MIN_VALUE;
                break;
            case PING_PONG:
                this.min = Integer.MIN_VALUE;
                break;
        }
    }


    // Track state to prevent unordered calls
    private boolean finishedTick = true;

    // Starts the transaction sending task
    public void start(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            this.transactionHandlers.removeIf(pinger -> {
                pinger.tickStart();
                return !pinger.isOpen();
            });

            this.finishedTick = false;
        }, 0, 1);
    }

    // Pass end of tick to all handlers
    public void endTick() {
        if (!this.finishedTick) {
            this.finishedTick = true;

            this.transactionHandlers.removeIf(pinger -> {
                pinger.tickEnd();
                return !pinger.isOpen();
            });
        }
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public void createTransactionHandler(Player player) {
        try {
            Channel channel = MinecraftUtil.getChannelFromPlayer(player);
            TransactionHandler handler = new TransactionHandler(player, channel, this.direction, this.min, this.max);

            // Use our own handler to listen to incoming transactions
            channel.pipeline().addBefore("packet_handler", "pledge_packet_handler", new PledgePacketHandler(handler));

            this.transactionHandlers.add(handler);
        } catch (Exception e) {
            PledgeImpl.LOGGER.severe("Could not create a transaction handler for " + player.getName() + "!");
            e.printStackTrace();
        }
    }

    public void addListener(TransactionListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(TransactionListener listener) {
        this.listeners.remove(listener);
    }

    public void callEvent(TransactionEventType type, TransactionEvent event) {
        this.listeners.forEach(listener -> type.processEvent(listener, event));
    }
}
