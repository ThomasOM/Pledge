package dev.thomazz.pledge.network;

import dev.thomazz.pledge.packet.PacketFiltering;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.Queue;

// Prevents asynchronously sent packets to land outside the start and end ping interval
@Getter
@RequiredArgsConstructor
public class NetworkPingHandler extends ChannelOutboundHandlerAdapter {
    private final Queue<NetworkMessage> messageQueue = new ArrayDeque<>();
    private final boolean consolidating;

    private boolean loggedIn = false;
    private boolean open = true;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // Start with login packet in game state
        if (PacketFiltering.isLoginPacket(msg)) {
            this.loggedIn = true;
            super.write(ctx, msg, promise);
            return;
        }

        // Let whitelisted packets pass
        if (PacketFiltering.isWhitelistedFromQueue(msg)) {
            super.write(ctx, msg, promise);
            return;
        }

        // Check if started, some packets are whitelisted from being queued
        if (this.consolidating && this.loggedIn && !this.open) {
            this.messageQueue.add(NetworkMessage.of(msg, promise));
            return;
        }

        // If not using message queue
        super.write(ctx, msg, promise);
    }

    public void open() {
        this.open = true;
    }

    public void close() {
        this.open = false;
    }

    public void drain(ChannelHandlerContext ctx) {
        while (!this.messageQueue.isEmpty()) {
            NetworkMessage message = this.messageQueue.poll();
            ctx.write(message.getMessage(), message.getPromise());
        }

        ctx.flush();
    }
}
