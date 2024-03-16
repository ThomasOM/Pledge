package dev.thomazz.pledge.pinger;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.packet.PingPacketProvider;
import dev.thomazz.pledge.pinger.data.Ping;
import dev.thomazz.pledge.pinger.data.PingData;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
public abstract class AbstractClientPinger implements ClientPinger {
    protected final Map<Player, PingData> pingDataMap = new LinkedHashMap<>();
    protected final List<ClientPingerListener> pingListeners = new ArrayList<>();

    protected final PledgeImpl api;
    protected final int startId;
    protected final int endId;

    protected Predicate<Player> playerFilter = player -> true;

    protected AbstractClientPinger(PledgeImpl api, int startId, int endId) {
        this.api = api;

        PingPacketProvider provider = api.getPacketProvider();
        int upperBound = provider.getUpperBound();
        int lowerBound = provider.getLowerBound();

        this.startId = Math.max(Math.min(upperBound, startId), lowerBound);
        this.endId = Math.max(Math.min(upperBound, endId), lowerBound);

        if (this.startId != startId) {
            this.api.getLogger().warning("Changed start ID to fit bounds: " + startId + " -> " + this.startId);
        }

        if (this.endId != endId) {
            this.api.getLogger().warning("Changed end ID to fit bounds: " + endId + " -> " + this.endId);
        }
    }

    @Override
    public int startId() {
        return this.startId;
    }

    @Override
    public int endId() {
        return this.endId;
    }

    @Override
    public void filter(Predicate<Player> condition) {
        this.playerFilter = condition;
    }

    @Override
    public void attach(ClientPingerListener listener) {
        this.pingListeners.add(listener);
    }

    public void registerPlayer(Player player) {
        if (this.playerFilter.test(player)) {
            this.pingDataMap.put(player, new PingData(player,this));
        }
    }

    public void unregisterPlayer(Player player) {
        this.pingDataMap.remove(player);
    }

    protected void ping(Player player, Ping ping) {
        this.api.getChannel(player).ifPresent(
            channel -> channel.eventLoop().execute(() -> {
                this.api.sendPing(player, ping.getId());
                this.getPingData(player).ifPresent(data -> data.offer(ping));
                this.onSend(player, ping);
            })
        );
    }

    public boolean isInRange(int id) {
        return id >= Math.min(this.startId, this.endId) && id <= Math.max(this.startId, this.endId);
    }

    public Optional<PingData> getPingData(Player player) {
        return Optional.of(this.pingDataMap.get(player));
    }

    protected void onSend(Player player, Ping ping) {
        switch (ping.getOrder()) {
            case TICK_START:
                this.onSendStart(player, ping.getId());
                break;
            case TICK_END:
                this.onSendEnd(player, ping.getId());
                break;
        }
    }

    public void onReceive(Player player, Ping ping) {
        switch (ping.getOrder()) {
            case TICK_START:
                this.onReceiveStart(player, ping.getId());
                break;
            case TICK_END:
                this.onReceiveEnd(player, ping.getId());
                break;
        }
    }

    protected void onSendStart(Player player, int id) {
        this.pingListeners.forEach(listener -> listener.onPingSendStart(player, id));
    }

    protected void onSendEnd(Player player, int id) {
        this.pingListeners.forEach(listener -> listener.onPingSendEnd(player, id));
    }

    protected void onReceiveStart(Player player, int id) {
        this.pingListeners.forEach(listener -> listener.onPongReceiveStart(player, id));
    }

    protected void onReceiveEnd(Player player, int id) {
        this.pingListeners.forEach(listener -> listener.onPongReceiveEnd(player, id));
    }

    public abstract void tickStart();

    public abstract void tickEnd();
}
