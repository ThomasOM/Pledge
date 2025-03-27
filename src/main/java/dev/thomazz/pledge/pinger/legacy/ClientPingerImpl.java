package dev.thomazz.pledge.pinger.legacy;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.event.PongReceiveEvent;
import dev.thomazz.pledge.event.TickEndEvent;
import dev.thomazz.pledge.event.TickStartEvent;
import dev.thomazz.pledge.network.NetworkPingHandler;
import dev.thomazz.pledge.packet.ping.PingPacketProvider;
import dev.thomazz.pledge.pinger.legacy.data.Ping;
import dev.thomazz.pledge.pinger.legacy.data.PingData;
import dev.thomazz.pledge.pinger.legacy.data.PingOrder;
import dev.thomazz.pledge.util.ChannelUtils;
import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
public class ClientPingerImpl implements ClientPinger, Listener {
    private final Map<Player, PingData> pingDataMap = new LinkedHashMap<>();
    private final List<ClientPingerListener> pingListeners = new ArrayList<>();

    private final PledgeImpl api;

    private final int startId;
    private final int endId;
    private final boolean consolidating;

    private Predicate<Player> playerFilter = player -> true;

    public ClientPingerImpl(PledgeImpl api, ClientPingerOptions options) {
        this.api = api;

        PingPacketProvider provider = api.getPacketProvider();
        int upperBound = provider.getUpperBound();
        int lowerBound = provider.getLowerBound();

        this.startId = Math.max(Math.min(upperBound, options.getStartId()), lowerBound);
        this.endId = Math.max(Math.min(upperBound, options.getEndId()), lowerBound);
        this.consolidating = options.isConsolidatePackets();

        if (this.startId != options.getStartId()) {
            this.api.getLogger().warning("Changed start ID to fit bounds: " + options.getStartId() + " -> " + this.startId);
        }

        if (this.endId != options.getEndId()) {
            this.api.getLogger().warning("Changed end ID to fit bounds: " + options.getEndId() + " -> " + this.endId);
        }

        this.api.registerListener(this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (this.playerFilter.test(player)) {
            this.injectPlayer(player);
            this.pingDataMap.put(player, new PingData(this, player));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.pingDataMap.remove(player);
        this.ejectPlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTickStart(TickStartEvent ignored) {
        for (PingData data : this.pingDataMap.values()) {
            this.api.getChannel(data.getPlayer()).ifPresent(channel ->
                ChannelUtils.runInEventLoop(channel, () -> this.tickStartPingData(data, channel))
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTickEnd(TickEndEvent ignored) {
        for (PingData data : this.pingDataMap.values()) {
            this.api.getChannel(data.getPlayer()).ifPresent(channel ->
                ChannelUtils.runInEventLoop(channel, () -> this.tickEndPingData(data, channel))
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPongReceive(PongReceiveEvent event) {
        Player player = event.getPlayer();
        int id = event.getId();

        // Check if in range first
        if (!this.isInRange(id)) {
            return;
        }

        // Get
        this.getPingData(player)
            .flatMap(data -> data.confirm(id))
            .ifPresent(pong -> this.onReceive(player, pong));
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
    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void filter(Predicate<Player> condition) {
        this.playerFilter = condition;
    }

    @Override
    public void attach(ClientPingerListener listener) {
        this.pingListeners.add(listener);
    }

    public boolean isInRange(int id) {
        return id >= Math.min(this.startId, this.endId) && id <= Math.max(this.startId, this.endId);
    }

    public Optional<PingData> getPingData(Player player) {
        return Optional.ofNullable(this.pingDataMap.get(player));
    }

    private void injectPlayer(Player player) {
        this.api.getChannel(player).ifPresent(channel ->
            ChannelUtils.runInEventLoop(
                channel,
                () -> channel.pipeline().addLast(
                    "pledge_tick_consolidator",
                    new NetworkPingHandler(this.consolidating)
                )
            )
        );
    }

    private void ejectPlayer(Player player) {
        this.api.getChannel(player).ifPresent(channel ->
            ChannelUtils.runInEventLoop(
                channel,
                () -> channel.pipeline().remove(NetworkPingHandler.class)
            )
        );
    }

    private void sendPing(Player player, Channel channel, Ping ping) {
        // Should always run in channel event loop
        if (!channel.eventLoop().inEventLoop()) {
            throw new IllegalStateException("Tried to run ping outside event loop!");
        }

        this.api.sendPingRaw(player, channel, ping.getId());
        this.getPingData(player).ifPresent(data -> data.offer(ping));
        this.onSend(player, ping);
    }

    private void onSend(Player player, Ping ping) {
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

    private void tickStartPingData(PingData data, Channel channel) {
        NetworkPingHandler pingHandler = channel.pipeline().get(NetworkPingHandler.class);
        if (pingHandler == null) {
            return;
        }

        if (pingHandler.isConsolidating()) {
            pingHandler.open();
            this.sendPing(data.getPlayer(), channel, new Ping(PingOrder.TICK_START, data.pullId()));
            pingHandler.drain(channel.pipeline().lastContext());
        } else {
            this.sendPing(data.getPlayer(), channel, new Ping(PingOrder.TICK_START, data.pullId()));
        }
    }

    private void tickEndPingData(PingData data, Channel channel) {
        NetworkPingHandler pingHandler = channel.pipeline().get(NetworkPingHandler.class);
        if (pingHandler == null) {
            return;
        }

        this.sendPing(data.getPlayer(), channel, new Ping(PingOrder.TICK_END, data.pullId()));

        if (pingHandler.isConsolidating()) {
            pingHandler.close();
        }
    }
}
