package dev.thomazz.pledge.inject.net;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.transaction.TransactionHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class BukkitChannelInitializer extends EnclosingChannelInitializer {
    public BukkitChannelInitializer(ChannelInitializer<?> initializer) {
        super(initializer);
    }

    @Override
    public void onInitChannel(SocketChannel channel) {
        // Create transaction handler and add our post handler for incoming client transactions
        TransactionHandler handler = PledgeImpl.INSTANCE.getTransactionManager().createTransactionHandler(channel);
        channel.pipeline().addAfter("packet_handler", "post_packet_handler", new PostPacketChannelHandler(handler));
    }
}
