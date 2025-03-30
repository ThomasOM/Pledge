package dev.thomazz.pledge.pinger.impl;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.event.PongReceiveEvent;
import dev.thomazz.pledge.network.LegacyNetworkPingHandler;
import dev.thomazz.pledge.pinger.AbstractPinger;
import dev.thomazz.pledge.pinger.PingerEventContext;
import dev.thomazz.pledge.pinger.PingerOptions;
import dev.thomazz.pledge.util.ChannelUtils;
import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public class LegacyPingerImpl extends AbstractPinger {
    private final Queue<PingerEventContext.Order> tickOrderQueue = new ConcurrentLinkedQueue<>();

    private final boolean consolidating;
    private final LegacyNetworkPingHandler pingHandler;

    public LegacyPingerImpl(PledgeImpl api, Player player, Channel channel, PingerOptions options) {
        super(api, player, channel, options);

        this.consolidating = options.isConsolidatePackets();
        this.pingHandler = new LegacyNetworkPingHandler(this.consolidating);

        this.channel.pipeline().addLast("pledge_tick_consolidator", this.pingHandler);
    }

    @Override
    public void onTickStart() {
        if (!ChannelUtils.ensureInEventLoop(this.channel, this::onTickStart)) {
            return;
        }

        if (this.pingHandler.isConsolidating()) {
            this.pingHandler.open();
            this.api.sendPingRaw(this.player, this.channel, this.pullId());
            this.pingHandler.drain(this.channel.pipeline().lastContext());
        } else {
            this.api.sendPingRaw(this.player, this.channel, this.pullId());
        }

        this.tickOrderQueue.add(PingerEventContext.Order.TICK_START);
    }

    @Override
    public void onTickEnd() {
        if (!ChannelUtils.ensureInEventLoop(this.channel, this::onTickEnd)) {
            return;
        }

        this.api.sendPingRaw(this.player, this.channel, this.pullId());

        if (this.pingHandler.isConsolidating()) {
            this.pingHandler.close();
        }

        this.tickOrderQueue.add(PingerEventContext.Order.TICK_END);
    }

    @Override
    public void onPongReceive(PongReceiveEvent event) {
        PingerEventContext.Order order = this.tickOrderQueue.poll();
        boolean valid = this.confirm(event.getId());

        event.setContext(
            PingerEventContext.builder()
                .order(order)
                .valid(valid)
                .build()
        );
    }

    @Override
    public void cleanUp() {
        this.channel.pipeline().remove(LegacyNetworkPingHandler.class);
    }
}
