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
    @SuppressWarnings("ConstantConditions")
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        try {
            switch (PacketUtil.MODE) {
                case TRANSACTION:
                    if (PacketUtil.IN_TRANSACTION_CLASS.equals(object.getClass())) {
                        TransactionHandler handler = this.transactionHandler.get();
                        if (handler != null && (int) PacketUtil.IN_WINDOW_FIELD_GET.invoke(object) == 0) {
                            handler.handleIncomingTransaction((short) PacketUtil.IN_ACTION_FIELD_GET.invoke(object));
                        }
                    }
                    break;
                case PING_PONG:
                    if (PacketUtil.PONG_CLASS.equals(object.getClass())) {
                        TransactionHandler handler = this.transactionHandler.get();
                        if (handler != null) {
                            handler.handleIncomingTransaction((int) PacketUtil.PONG_ID_FIELD_GET.invoke(object));
                        }
                    }
                    break;
            }
        } catch (Throwable throwable) {
            PledgeImpl.LOGGER.severe("Could not read incoming packet!");
            throwable.printStackTrace();
        }

        super.channelRead(channelHandlerContext, object);
    }
}
