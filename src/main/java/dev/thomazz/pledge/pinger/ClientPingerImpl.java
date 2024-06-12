package dev.thomazz.pledge.pinger;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.network.NetworkPacketConsolidator;
import dev.thomazz.pledge.packet.PingPacketProvider;
import dev.thomazz.pledge.pinger.data.Ping;
import dev.thomazz.pledge.pinger.data.PingData;
import dev.thomazz.pledge.pinger.data.PingOrder;
import dev.thomazz.pledge.util.ChannelUtils;
import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
public class ClientPingerImpl implements ClientPinger {
    protected final Map<Player, PingData> pingDataMap = new LinkedHashMap<>();
    protected final List<ClientPingerListener> pingListeners = new ArrayList<>();

    protected final PledgeImpl api;
    protected final int startId;
    protected final int endId;
    protected final boolean consolidate;

    protected Predicate<Player> playerFilter = player -> true;

    public ClientPingerImpl(PledgeImpl api, ClientPingerOptions options) {
        this.api = api;

        PingPacketProvider provider = api.getPacketProvider();
        int upperBound = provider.getUpperBound();
        int lowerBound = provider.getLowerBound();

        this.startId = Math.max(Math.min(upperBound, options.getStartId()), lowerBound);
        this.endId = Math.max(Math.min(upperBound, options.getEndId()), lowerBound);
        this.consolidate = options.isConsolidatePackets();

        if (this.startId != options.getStartId()) {
            this.api.getLogger().warning("Changed start ID to fit bounds: " + startId + " -> " + this.startId);
        }

        if (this.endId != options.getEndId()) {
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
            this.injectPlayer(player);
            this.pingDataMap.put(player, new PingData(player,this));
        }
    }

    public void unregisterPlayer(Player player) {
        this.pingDataMap.remove(player);
        this.ejectPlayer(player);
    }

    protected void injectPlayer(Player player) {
        // Only inject consolidator when necessary
        if (!this.consolidate) {
            return;
        }

        this.api.getChannel(player).ifPresent(channel ->
            ChannelUtils.runInEventLoop(channel,
                () -> channel.pipeline().addLast("pledge_tick_consolidator", new NetworkPacketConsolidator())
            )
        );
    }

    protected void ejectPlayer(Player player) {
        // Only inject consolidator when necessary
        if (!this.consolidate) {
            return;
        }

        this.api.getChannel(player).ifPresent(channel ->
            ChannelUtils.runInEventLoop(channel,
                () -> channel.pipeline().remove(NetworkPacketConsolidator.class)
            )
        );
    }

    // Note: Should run in channel event loop
    protected void ping(Player player, Channel channel, Ping ping) {
        if (!channel.eventLoop().inEventLoop()) {
            throw new IllegalStateException("Tried to run ping outside event loop!");
        }

        this.api.sendPingRaw(player, channel, ping.getId());
        this.getPingData(player).ifPresent(data -> data.offer(ping));
        this.onSend(player, ping);
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

    public void tickStart() {
        // Only inject consolidator when necessary
        if (!this.consolidate) {
            return;
        }

        this.pingDataMap.forEach((player, data) ->
            this.api.getChannel(player).ifPresent(channel ->
                ChannelUtils.runInEventLoop(channel, () -> {
                    NetworkPacketConsolidator consolidator = channel.pipeline().get(NetworkPacketConsolidator.class);
                    if (consolidator != null) {
                        consolidator.open();
                        this.ping(player, channel, new Ping(PingOrder.TICK_START, data.pullId()));
                        consolidator.drain(channel.pipeline().lastContext());
                    }
                })
            )
        );
    }

    public void tickEnd() {
        // Only inject consolidator when necessary
        if (!this.consolidate) {
            return;
        }

        this.pingDataMap.forEach((player, data) ->
            this.api.getChannel(player).ifPresent(channel ->
                ChannelUtils.runInEventLoop(channel, () -> {
                    NetworkPacketConsolidator consolidator = channel.pipeline().get(NetworkPacketConsolidator.class);
                    if (consolidator != null) {
                        this.ping(player, channel, new Ping(PingOrder.TICK_END, data.pullId()));
                        consolidator.close();
                    }
                })
            )
        );
    }
}
