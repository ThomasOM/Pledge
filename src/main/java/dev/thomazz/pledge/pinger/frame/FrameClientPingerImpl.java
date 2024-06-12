package dev.thomazz.pledge.pinger.frame;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.network.queue.MessageQueueHandler;
import dev.thomazz.pledge.network.queue.MessageQueuePrimer;
import dev.thomazz.pledge.network.queue.QueueMode;
import dev.thomazz.pledge.pinger.ClientPingerImpl;
import dev.thomazz.pledge.pinger.ClientPingerOptions;
import dev.thomazz.pledge.pinger.data.Ping;
import dev.thomazz.pledge.pinger.data.PingData;
import dev.thomazz.pledge.pinger.data.PingOrder;
import dev.thomazz.pledge.pinger.frame.data.Frame;
import dev.thomazz.pledge.pinger.frame.data.FrameData;
import dev.thomazz.pledge.util.ChannelUtils;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FrameClientPingerImpl extends ClientPingerImpl implements FrameClientPinger {
    private final Map<Player, FrameData> frameDataMap = new LinkedHashMap<>();
    private final List<FrameClientPingerListener> frameListener = new ArrayList<>();

    public FrameClientPingerImpl(PledgeImpl clientPing, ClientPingerOptions options) {
        super(clientPing, options);
    }

    @Override
    public void attach(FrameClientPingerListener listener) {
        super.attach(listener);
        this.frameListener.add(listener);
    }

    @Override
    public void registerPlayer(Player player) {
        super.registerPlayer(player);
        this.frameDataMap.put(player, new FrameData());
    }

    @Override
    public void unregisterPlayer(Player player) {
        super.unregisterPlayer(player);
        this.frameDataMap.remove(player);
    }

    @Override
    protected void injectPlayer(Player player) {
        MessageQueueHandler queueHandler = new MessageQueueHandler();
        MessageQueuePrimer queuePrimer = new MessageQueuePrimer(queueHandler);
        this.api.getChannel(player).ifPresent(channel ->
            ChannelUtils.runInEventLoop(channel, () ->
                channel.pipeline()
                    .addAfter("prepender", "pledge_queue_handler", queueHandler)
                    .addLast("pledge_queue_primer", queuePrimer)
            )
        );
    }

    @Override
    protected void ejectPlayer(Player player) {
        this.api.getChannel(player).ifPresent(channel ->
            ChannelUtils.runInEventLoop(channel, () -> {
                channel.pipeline().remove(MessageQueueHandler.class);
                channel.pipeline().remove(MessageQueuePrimer.class);
            })
        );
    }

    @Override
    public void tickStart() {
        // NO-OP
    }

    @Override
    public void tickEnd() {
        this.frameDataMap.forEach(this::trySendPings);
    }

    @Override
    protected void onReceiveStart(Player player, int id) {
        super.onReceiveStart(player, id);

        FrameData data = this.frameDataMap.get(player);
        if (data != null && this.frameListener != null) {
            data.matchStart(id).ifPresent(
                frame -> this.frameListener.forEach(listener -> listener.onFrameReceiveStart(player, frame))
            );
        }
    }

    @Override
    protected void onReceiveEnd(Player player, int id) {
        super.onReceiveEnd(player, id);

        FrameData data = this.frameDataMap.get(player);
        if (data != null && this.frameListener != null) {
            data.matchEnd(id).ifPresent(
                frame -> {
                    this.frameListener.forEach(listener -> listener.onFrameReceiveEnd(player, frame));
                    data.popFrame();
                }
            );
        }
    }

    @Override
    public synchronized Frame getOrCreate(Player player) {
        PingData pingData = this.pingDataMap.get(player);
        FrameData frameData = this.frameDataMap.get(player);

        Objects.requireNonNull(pingData);
        Objects.requireNonNull(frameData);

        if (!frameData.hasFrame()) {
            frameData.setFrame(this.createFrame(player, pingData));
        }

        return frameData.getFrame();
    }

    public Optional<FrameData> getFrameData(Player player) {
        return Optional.ofNullable(this.frameDataMap.get(player));
    }

    private void trySendPings(Player player, FrameData frameData) {
        Optional<Frame> optionalFrame = frameData.continueFrame();

        this.api.getChannel(player).filter(Channel::isOpen).ifPresent(channel ->
            ChannelUtils.runInEventLoop(channel, () -> {
                try {
                    MessageQueueHandler handler = channel.pipeline().get(MessageQueueHandler.class);
                    if (handler != null) {
                        if (optionalFrame.isPresent()) {
                            Frame frame = optionalFrame.get();
                            this.frameListener.forEach(listener -> listener.onFrameSend(player, frame));

                            // Wrap by ping packets
                            handler.setMode(QueueMode.ADD_FIRST);
                            this.ping(player, channel, new Ping(PingOrder.TICK_START, frame.getStartId()));
                            handler.setMode(QueueMode.ADD_LAST);
                            this.ping(player, channel, new Ping(PingOrder.TICK_END, frame.getEndId()));
                        }

                        handler.drain(channel.pipeline().context(handler));
                    }
                } catch (Exception ex) {
                    this.api.getLogger().severe("Unable to drain message queue from player: " + player.getName());
                    ex.printStackTrace();
                }
            })
        );
    }

    private Frame createFrame(Player player, PingData data) {
        Frame frame = new Frame(data.pullId(), data.pullId());
        this.frameListener.forEach(listener -> listener.onFrameCreate(player, frame));
        return frame;
    }
}
