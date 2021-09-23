package dev.thomazz.pledge.transaction;

import dev.thomazz.pledge.api.Direction;
import dev.thomazz.pledge.api.event.TransactionEvent;
import dev.thomazz.pledge.api.event.TransactionListener;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TransactionManager {
    private final List<TransactionHandler> transactionHandlers = new ArrayList<>();
    private final List<TransactionListener> listeners = new CopyOnWriteArrayList<>();

    private Direction direction;
    private short min;
    private short max;

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

    public void setRange(short min, short max) {
        this.min = min;
        this.max = max;
    }

    public TransactionHandler createTransactionHandler(Channel channel) {
        TransactionHandler handler = new TransactionHandler(channel, this.direction, this.min, this.max);
        this.transactionHandlers.add(handler);
        return handler;
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
