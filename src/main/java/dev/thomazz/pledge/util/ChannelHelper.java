package dev.thomazz.pledge.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class ChannelHelper {
    private final Method BYTE_ENCODE = ChannelHelper.getByteEncodeMethod();
    private final Method MESSAGE_ENCODE = ChannelHelper.getMessageEncodeMethod();

    public ByteBuf encodeAndCompress(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Encode packet
        MessageToByteEncoder<?> encoder = (MessageToByteEncoder<?>) ctx.pipeline().get("encoder");
        ByteBuf buf1 = ctx.alloc().buffer();
        ChannelHelper.BYTE_ENCODE.invoke(encoder, ctx, msg, buf1);

        // ViaVersion support
        MessageToMessageEncoder<?> via = (MessageToMessageEncoder<?>) ctx.pipeline().get("via-encoder");
        ByteBuf buf2 = buf1;
        if (via != null) {
            List<Object> list = new ArrayList<>(1);
            ChannelHelper.MESSAGE_ENCODE.invoke(via, ctx, buf1, list);
            buf2 = (ByteBuf) list.get(0);
        }

        // Compression support
        MessageToByteEncoder<?> compress = (MessageToByteEncoder<?>) ctx.pipeline().get("compress");
        ByteBuf buf3 = buf2;
        if (compress != null) {
            buf3 = ctx.alloc().buffer();
            ChannelHelper.BYTE_ENCODE.invoke(compress, ctx, buf2, buf3);
        }

        return buf3;
    }

    private Method getByteEncodeMethod() {
        try {
            Method method = MessageToByteEncoder.class.getDeclaredMethod("encode", ChannelHandlerContext.class, Object.class, ByteBuf.class);
            method.setAccessible(true);
            return method;
        } catch (Exception ex) {
            throw new RuntimeException("Could not find encode method'!", ex);
        }
    }

    private Method getMessageEncodeMethod() {
        try {
            Method method = MessageToMessageEncoder.class.getDeclaredMethod("encode", ChannelHandlerContext.class, Object.class, List.class);
            method.setAccessible(true);
            return method;
        } catch (Exception ex) {
            throw new RuntimeException("Could not find encode method'!", ex);
        }
    }
}
