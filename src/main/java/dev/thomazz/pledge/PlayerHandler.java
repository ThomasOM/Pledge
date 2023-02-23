package dev.thomazz.pledge;

import dev.thomazz.pledge.api.PacketFrame;
import dev.thomazz.pledge.api.event.ErrorType;
import dev.thomazz.pledge.api.event.PacketFrameCreateEvent;
import dev.thomazz.pledge.api.event.PacketFrameTimeoutEvent;
import dev.thomazz.pledge.api.event.ReceiveType;
import dev.thomazz.pledge.api.event.PacketFrameErrorEvent;
import dev.thomazz.pledge.api.event.PacketFrameReceiveEvent;
import dev.thomazz.pledge.network.delegation.DelegateChannelFactory;
import dev.thomazz.pledge.network.handler.PacketFrameInboundHandler;
import dev.thomazz.pledge.network.handler.PacketFrameOutboundHandler;
import dev.thomazz.pledge.network.handler.PacketFrameOutboundQueueHandler;
import dev.thomazz.pledge.packet.PacketProvider;
import dev.thomazz.pledge.util.MinecraftUtil;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class PlayerHandler {
    private final Queue<PacketFrame> frameQueue = new ConcurrentLinkedQueue<>();

    @Getter
    private final Player player;
    private final Channel original;
    private final Channel channel;

    private final int rangeStart;
    private final int rangeEnd;

    private PacketFrameOutboundHandler netHandler;

    private int id;
    private PacketFrame nextFrame;
    private PacketFrame receivingFrame;

    @Getter
    private int creationTicks;
    private int waitingTicks;
    private boolean timedOut;

    public PlayerHandler(Player player) throws Exception {
        this.player = player;

        this.original = MinecraftUtil.getChannel(player);
        this.channel = DelegateChannelFactory.buildDelegateChannel(this.original);
        MinecraftUtil.setChannel(player, this.channel);

        PledgeImpl pledge = PledgeImpl.getInstance();
        this.id = this.rangeStart = pledge.getRangeStart();
        this.rangeEnd = pledge.getRangeEnd();
    }

    public void inject(PledgeImpl pledge) {
        PacketProvider provider = pledge.getPacketProvider();

        // Create new channel handlers
        PacketFrameInboundHandler inbound = new PacketFrameInboundHandler(this, provider);
        PacketFrameOutboundQueueHandler queueHandler = new PacketFrameOutboundQueueHandler();
        this.netHandler = new PacketFrameOutboundHandler(this, provider, queueHandler);

        // We want to be right after the encoder and decoder so there's no interference with other packet listeners
        this.channel.eventLoop().execute(() -> {
            this.channel.pipeline().addAfter("decoder", PacketFrameInboundHandler.HANDLER_NAME, inbound);
            this.channel.pipeline().addAfter("prepender", PacketFrameOutboundQueueHandler.HANDLER_NAME, queueHandler);
            this.channel.pipeline().addAfter("encoder", PacketFrameOutboundHandler.HANDLER_NAME, this.netHandler);
        });
    }

    private int getAndUpdateId() {
        int previous = this.id;

        int increment = Integer.compare(this.rangeEnd - this.rangeStart, 0);
        this.id += increment;
        if (this.rangeEnd > this.rangeStart ? this.id > this.rangeEnd : this.id < this.rangeEnd) {
            this.id = this.rangeStart;
        }

        return previous;
    }

    private void callEvent(Event event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    private void resetWaitTicks() {
        this.waitingTicks = 0;
        this.timedOut = false;
    }

    public void tickStart() {
        // Only increment wait ticks when actually waiting for a frame, otherwise we can just reset wait ticks
        PacketFrame waiting = this.frameQueue.peek();
        if (waiting != null) {
            PledgeImpl pledge = PledgeImpl.getInstance();

            // Make sure that we don't spam call the event and wait for the next reset
            if (++this.waitingTicks > pledge.getTimeoutTicks() && !this.timedOut) {
                this.callEvent(new PacketFrameTimeoutEvent(this.player, waiting));
                this.timedOut = true;
            }
        } else {
            this.resetWaitTicks();
        }

        // Increment ticks since last frame was created
        this.creationTicks++;
    }

    public void tickEnd() {
        // Make sure to offer the next frame to the handler and the awaiting frame queue
        if (this.nextFrame != null) {
            this.frameQueue.offer(this.nextFrame);
            this.netHandler.flushFrame(this.nextFrame);
            this.nextFrame = null;
        }

        // Flush original channel at the end of the tick since the delegate does not flush
        this.original.flush();
    }

    // Processes incoming ids from netty thread
    public boolean processId(int id) {
        // Make sure the ID is within the range
        if (id < Math.min(this.rangeStart, this.rangeEnd) || id > Math.max(this.rangeStart, this.rangeEnd)) {
            return false;
        }

        if (this.receivingFrame == null) {
            PacketFrame frame = this.frameQueue.peek();
            if (frame != null && frame.getId1() == id) {
                this.receivingFrame = this.frameQueue.poll();
                this.callEvent(new PacketFrameReceiveEvent(this.player, frame, ReceiveType.RECEIVE_START));
            } else {
                this.callEvent(new PacketFrameErrorEvent(this.player, frame, ErrorType.MISSING_FRAME));
            }
        } else {
            if (this.receivingFrame.getId2() == id) {
                this.callEvent(new PacketFrameReceiveEvent(this.player, this.receivingFrame, ReceiveType.RECEIVE_END));
                this.receivingFrame = null;
            } else {
                this.callEvent(new PacketFrameErrorEvent(this.player, this.receivingFrame, ErrorType.INCOMPLETE_FRAME));
            }
        }

        // Reset waiting ticks because we received a correct response
        this.resetWaitTicks();
        return true;
    }

    public Optional<PacketFrame> getNextFrame() {
        return Optional.ofNullable(this.nextFrame);
    }

    // Creates a new frame for the current tick if there is not already one
    public PacketFrame createNextFrame() {
        if (this.nextFrame == null) {
            this.nextFrame = new PacketFrame(this.getAndUpdateId(), this.getAndUpdateId());
            this.callEvent(new PacketFrameCreateEvent(this.player, this.nextFrame));
        }

        // Reset creation ticks
        this.creationTicks = 0;

        return this.nextFrame;
    }

    public void cleanUp() {
        try {
            // Set back original channel instead of the delegate
            MinecraftUtil.setChannel(this.player, this.original);

            // Try to remove the channel handlers
            this.channel.pipeline().remove("pledge_frame_outbound");
            this.channel.pipeline().remove("pledge_frame_inbound");
        } catch (NoSuchElementException ignored) {
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
