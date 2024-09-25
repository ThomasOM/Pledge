package dev.thomazz.pledge.pinger.data;

import dev.thomazz.pledge.pinger.ClientPingerImpl;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public class PingData {
    private final Queue<Ping> expectingIds = new ConcurrentLinkedQueue<>();
    private final ClientPingerImpl pinger;
    private final Player player;

    private boolean validated = false;
    private int id;

    public PingData(ClientPingerImpl pinger, Player player) {
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

    public void offer(Ping ping) {
        this.expectingIds.add(ping);
    }

    public Optional<Ping> confirm(int id) {
        Ping ping = this.expectingIds.peek();

        if (ping != null && ping.getId() == id) {
            this.expectingIds.poll();

            // Make sure to notify validation with the first correct ping received
            if (!this.validated) {
                this.pinger.getPingListeners().forEach(listener -> listener.onValidation(this.player, id));
                this.validated = true;
            }

            return Optional.of(ping);
        }

        // Notify listeners of an unexpected ping response from player
        this.pinger.getPingListeners().forEach(listener -> listener.onPongReceiveInvalid(this.player, id));
        return Optional.empty();
    }
}
