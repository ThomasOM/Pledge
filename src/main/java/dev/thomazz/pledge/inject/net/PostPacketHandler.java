package dev.thomazz.pledge.inject.net;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.transaction.TransactionHandler;
import dev.thomazz.pledge.util.PacketUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class PostPacketHandler extends SimpleChannelInboundHandler<Object> {
    private final Reference<TransactionHandler> transactionHandler;

    public PostPacketHandler(TransactionHandler transactionHandler) {
        // Don't want in memory channels to prevent the handler from getting collected
        this.transactionHandler = new WeakReference<>(transactionHandler);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object object) {
        if (PacketUtil.IN_TRANSACTION_CLASS.equals(object.getClass())) {
            try {
                // Handle transaction if the window ID is correct
                TransactionHandler handler = this.transactionHandler.get();
                if (handler != null && (int) PacketUtil.WINDOW_FIELD.get(object) == 0) {
                    handler.handleIncomingTransaction((short) PacketUtil.ACTION_FIELD.get(object));
                }
            } catch (Exception e) {
                PledgeImpl.LOGGER.severe("Could not read transaction packet!");
                e.printStackTrace();
            }
        }
    }
}
