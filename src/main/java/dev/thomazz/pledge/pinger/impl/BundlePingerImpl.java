package dev.thomazz.pledge.pinger.impl;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.event.PongReceiveEvent;
import dev.thomazz.pledge.pinger.AbstractPinger;
import dev.thomazz.pledge.pinger.PingerEventContext;
import dev.thomazz.pledge.pinger.PingerOptions;
import dev.thomazz.pledge.util.ChannelUtils;
import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@Getter
public class BundlePingerImpl extends AbstractPinger {
	public BundlePingerImpl(PledgeImpl api, Player player, Channel channel, PingerOptions options) {
		super(api, player, channel, options);
	}

	public void onTickStart() {
		ChannelUtils.runInEventLoop(this.channel, () -> this.api.sendPingRaw(this.player, this.channel, this.pullId()));
	}

	public void onTickEnd() {
		ChannelUtils.runInEventLoop(this.channel, () -> this.api.sendBundleRaw(this.player, this.channel));
	}

	@EventHandler
	public void onPongReceive(PongReceiveEvent event) {
		boolean valid = this.confirm(event.getId());
		event.setContext(PingerEventContext.builder().valid(valid).build());
	}
}
