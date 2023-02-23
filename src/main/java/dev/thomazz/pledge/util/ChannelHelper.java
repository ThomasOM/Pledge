package dev.thomazz.pledge.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;

@UtilityClass
public final class ChannelHelper {
    private final Method ENCODE = ChannelHelper.getEncodeMethod();

    public ByteBuf encodeAndCompress(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageToByteEncoder<?> encoder = (MessageToByteEncoder<?>) ctx.pipeline().get("encoder");
        ByteBuf encoded = ctx.alloc().buffer();
        ChannelHelper.ENCODE.invoke(encoder, ctx, msg, encoded);

        MessageToByteEncoder<?> compress = (MessageToByteEncoder<?>) ctx.pipeline().get("compress");

        ByteBuf out;
        if (compress != null) {
            out = ctx.alloc().buffer();
            ChannelHelper.ENCODE.invoke(compress, ctx, encoded, out);
        } else {
            out = encoded;
        }

        return out;
    }

    private Method getEncodeMethod() {
        try {
            Method method = MessageToByteEncoder.class.getDeclaredMethod("encode", ChannelHandlerContext.class, Object.class, ByteBuf.class);
            method.setAccessible(true);
            return method;
        } catch (Exception ex) {
            throw new RuntimeException("Could not find encode method'!", ex);
        }
    }
}
