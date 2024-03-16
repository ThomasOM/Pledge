package dev.thomazz.pledge.pinger.data;

import dev.thomazz.pledge.pinger.AbstractClientPinger;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class PingData {
    private final Queue<Ping> expectingIds = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean validated = new AtomicBoolean(false);
    private final Player player;
    private final AbstractClientPinger pinger;
    private volatile int id;

    public PingData(Player player, AbstractClientPinger pinger) {
        this.player = player;
        this.pinger = pinger;
        this.id = pinger.startId();
    }

    public int pullId() {
        int startId = this.pinger.startId();
        int endId = this.pinger.endId();

        boolean direction = endId - startId > 0;
        int oldId = this.id;
        int newId = oldId + (direction ? 1 : -1);

        if (direction ? newId > endId : newId < endId) {
            newId = startId;
        }

        this.id = newId;
        return oldId;
    }

    public void offer(@NotNull Ping ping) {
        this.expectingIds.add(ping);
    }

    public Optional<Ping> confirm(int id) {
        Ping ping = this.expectingIds.peek();

        if (ping != null && ping.getId() == id) {
            // Make sure to notify validation with the first correct ping received
            if (this.validated.compareAndSet(false, true)) {
                this.pinger.getPingListeners().forEach(listener -> listener.onValidation(this.player, id));
            }

            return Optional.ofNullable(this.expectingIds.poll());
        }

        return Optional.empty();
    }

    public boolean isValidated() {
        return this.validated.get();
    }
}
