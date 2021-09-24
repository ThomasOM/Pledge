package dev.thomazz.pledge.inject.net;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.transaction.TransactionHandler;
import dev.thomazz.pledge.util.PacketUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class PledgePacketHandler extends ChannelInboundHandlerAdapter {
    private final Reference<TransactionHandler> transactionHandler;

    public PledgePacketHandler(TransactionHandler transactionHandler) {
        // Don't want in memory channel handlers to prevent the handler from getting collected
        this.transactionHandler = new WeakReference<>(transactionHandler);
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        if (PacketUtil.IN_TRANSACTION_CLASS.equals(object.getClass())) {
            try {
                // Handle transaction if the window ID is correct
                TransactionHandler handler = this.transactionHandler.get();
                if (handler != null && (int) PacketUtil.IN_WINDOW_FIELD.get(object) == 0) {
                    handler.handleIncomingTransaction((short) PacketUtil.IN_ACTION_FIELD.get(object));
                }
            } catch (Exception e) {
                PledgeImpl.LOGGER.severe("Could not read transaction packet!");
                e.printStackTrace();
            }
        }

        super.channelRead(channelHandlerContext, object);
    }
}
