package dev.thomazz.pledge.pinger.bundle;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.event.PongReceiveEvent;
import dev.thomazz.pledge.event.TickEndEvent;
import dev.thomazz.pledge.event.TickStartEvent;
import dev.thomazz.pledge.packet.PacketBundling;
import dev.thomazz.pledge.packet.ping.PingPacketProvider;
import dev.thomazz.pledge.pinger.PingerOptions;
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

@Getter
public class BundlePingerImpl implements BundlePinger, Listener {
	private final List<BundlePingerListener> listeners = new ArrayList<>();
	private final Map<Player, BundlePingData> pingDataMap = new LinkedHashMap<>();

	private final PledgeImpl api;
	private final int startId;
	private final int endId;

	public BundlePingerImpl(PledgeImpl api, PingerOptions options) {
		this.api = api;

		PingPacketProvider provider = api.getPacketProvider();
		int upperBound = provider.getUpperBound();
		int lowerBound = provider.getLowerBound();

		this.startId = Math.max(Math.min(upperBound, options.getStartId()), lowerBound);
		this.endId = Math.max(Math.min(upperBound, options.getEndId()), lowerBound);

		if (this.startId != options.getStartId()) {
			this.api.getLogger().warning("Changed start ID to fit bounds: " + options.getStartId() + " -> " + this.startId);
		}

		if (this.endId != options.getEndId()) {
			this.api.getLogger().warning("Changed end ID to fit bounds: " + options.getEndId() + " -> " + this.endId);
		}

		this.api.registerListener(this);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		this.pingDataMap.put(player, new BundlePingData(this, player));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		this.pingDataMap.remove(player);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onTickStart(TickStartEvent ignored) {
		for (BundlePingData data : this.pingDataMap.values()) {
			this.api.getChannel(data.getPlayer()).ifPresent(channel ->
				ChannelUtils.runInEventLoop(channel, () -> this.tickStart(data, channel))
			);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onTickEnd(TickEndEvent ignored) {
		for (BundlePingData data : this.pingDataMap.values()) {
			this.api.getChannel(data.getPlayer()).ifPresent(channel ->
				ChannelUtils.runInEventLoop(channel, () -> this.tickEnd(channel))
			);
		}
	}

	@EventHandler
	void onPongReceive(PongReceiveEvent event) {
		Player player = event.getPlayer();
		int id = event.getId();

		if (this.pingDataMap.get(player).confirm(id)) {
			this.listeners.forEach(listener -> listener.onPongReceive(player, id));
		} else {
			this.listeners.forEach(listener -> listener.onPongReceiveInvalid(player, id));
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
	public void destroy() {
		HandlerList.unregisterAll(this);
	}

	@Override
	public void attach(BundlePingerListener listener) {
		this.listeners.add(listener);
	}

	private void tickStart(BundlePingData data, Channel channel) {
		this.api.sendPingRaw(data.getPlayer(), channel, data.pullId());
	}

	private void tickEnd(Channel channel) {
		try {
			channel.writeAndFlush(PacketBundling.buildBundlePacket());
		} catch (Exception ex) {
			this.api.getLogger().severe("Could not send bundle delimiter packet!");
		}
	}
}
