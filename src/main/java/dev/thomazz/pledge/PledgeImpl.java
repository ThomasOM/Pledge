package dev.thomazz.pledge;

import dev.thomazz.pledge.event.PingSendEvent;
import dev.thomazz.pledge.event.PongReceiveEvent;
import dev.thomazz.pledge.event.TickEndEvent;
import dev.thomazz.pledge.event.TickStartEvent;
import dev.thomazz.pledge.network.NetworkPongListener;
import dev.thomazz.pledge.packet.PacketProviderFactory;
import dev.thomazz.pledge.packet.PingPacketProvider;
import dev.thomazz.pledge.pinger.ClientPinger;
import dev.thomazz.pledge.pinger.ClientPingerImpl;
import dev.thomazz.pledge.pinger.frame.FrameClientPinger;
import dev.thomazz.pledge.pinger.frame.FrameClientPingerImpl;
import dev.thomazz.pledge.util.ChannelAccess;
import dev.thomazz.pledge.util.TickEndTask;
import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Getter
public class PledgeImpl implements Pledge, Listener {
    static PledgeImpl instance;

    private final Logger logger;
    private final PingPacketProvider packetProvider;

    private final BukkitTask startTask;
    private final TickEndTask endTask;

    private final List<ClientPingerImpl> clientPingers = new ArrayList<>();
    private final Map<Player, Channel> playerChannels = new HashMap<>();

    PledgeImpl(Plugin plugin) {
        this.logger = plugin.getLogger();
        this.packetProvider = PacketProviderFactory.buildPingProvider();

        PluginManager manager = plugin.getServer().getPluginManager();
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        this.startTask = scheduler.runTaskTimer(plugin, () -> manager.callEvent(new TickStartEvent()), 0L, 1L);
        this.endTask = TickEndTask.create(() -> manager.callEvent(new TickEndEvent()));

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void setupPlayer(Player player) {
        Channel channel = ChannelAccess.getChannel(player);
        this.playerChannels.put(player, channel);

        // Inject pong listener
        channel.pipeline().addBefore(
            "packet_handler",
            "pledge_packet_listener",
            new NetworkPongListener(this, player)
        );

        // Register to client pingers
        this.clientPingers.forEach(pinger -> pinger.registerPlayer(player));
    }

    private void teardownPlayer(Player player) {
        this.playerChannels.remove(player);

        // Unregister from client pingers
        this.clientPingers.forEach(pinger -> pinger.unregisterPlayer(player));
    }

    PledgeImpl start() {
        // Setup for all players
        Bukkit.getOnlinePlayers().forEach(this::setupPlayer);
        return this;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerLogin(PlayerLoginEvent event) {
        this.setupPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerQuit(PlayerQuitEvent event) {
        this.teardownPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onTickStart(TickStartEvent ignored) {
        this.clientPingers.forEach(ClientPingerImpl::tickStart);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onTickStart(TickEndEvent ignored) {
        this.clientPingers.forEach(ClientPingerImpl::tickEnd);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPongReceive(PongReceiveEvent event) {
        Player player = event.getPlayer();
        int id = event.getId();

        this.clientPingers.stream()
            .filter(pinger -> pinger.isInRange(id))
            .forEach(
                pinger -> pinger.getPingData(player)
                    .flatMap(data -> data.confirm(id))
                    .ifPresent(pong -> pinger.onReceive(player, pong))
            );
    }

    @Override
    public void sendPing(@NotNull Player player, int id) {
        try {
            PluginManager pluginManager = Bukkit.getServer().getPluginManager();
            Object packet = this.packetProvider.buildPacket(id);

            // Keep within ranges
            int max = Math.max(this.packetProvider.getUpperBound(), this.packetProvider.getLowerBound());
            int min = Math.min(this.packetProvider.getUpperBound(), this.packetProvider.getLowerBound());
            int pingId = Math.max(Math.min(id, max), min);

            // Run on channel event loop
            this.getChannel(player).ifPresent(channel -> {
                if (channel.eventLoop().inEventLoop()) {
                    pluginManager.callEvent(new PingSendEvent(player, id));
                    channel.writeAndFlush(packet);
                } else {
                    channel.eventLoop().execute(() -> {
                        pluginManager.callEvent(new PingSendEvent(player, pingId));
                        channel.writeAndFlush(packet);
                    });
                }
            });
        } catch (Exception ex) {
            this.logger.severe(String.format("Failed to send ping! Player:%s Id:%o", player.getName(), id));
            ex.printStackTrace();
        }
    }

    @Override
    public Optional<Channel> getChannel(@NotNull Player player) {
        return Optional.ofNullable(this.playerChannels.get(player));
    }

    @Override
    public ClientPinger createPinger(int startId, int endId) {
        ClientPingerImpl pinger = new ClientPingerImpl(this, startId, endId);
        this.clientPingers.add(pinger);
        return pinger;
    }

    @Override
    public FrameClientPinger createFramePinger(int startId, int endId) {
        FrameClientPingerImpl pinger = new FrameClientPingerImpl(this, startId, endId);
        this.clientPingers.add(pinger);
        return pinger;
    }

    @Override
    public void destroy() {
        if (!this.equals(PledgeImpl.instance)) {
            throw new IllegalStateException("API object not the same as current instance!");
        }

        // Teardown for all players
        Bukkit.getOnlinePlayers().forEach(this::teardownPlayer);

        HandlerList.unregisterAll(this);
        this.startTask.cancel();
        this.endTask.cancel();

        PledgeImpl.instance = null;
    }
}
