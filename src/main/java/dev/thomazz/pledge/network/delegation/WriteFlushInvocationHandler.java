package dev.thomazz.pledge.network.delegation;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class WriteFlushInvocationHandler implements InvocationHandler {
    private static final Method writeAndFlush = WriteFlushInvocationHandler.channelMethod("writeAndFlush", Object.class);
    private static final Method writeAndFlushAlt = WriteFlushInvocationHandler.channelMethod("writeAndFlush", Object.class, ChannelPromise.class);
    private static final Method flush = WriteFlushInvocationHandler.channelMethod("flush");

    private final Channel original;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Replace all writeAndFlush calls with write calls and do not allow explicit flush calls
        if (WriteFlushInvocationHandler.writeAndFlush.equals(method) || WriteFlushInvocationHandler.writeAndFlushAlt.equals(method)) {
            if (args.length > 1) {
                return this.original.write(args[0], (ChannelPromise) args[1]);
            } else {
                return this.original.write(args[0]);
            }
        } else if (!WriteFlushInvocationHandler.flush.equals(method)){
            return method.invoke(this.original, args);
        }

        return null;
    }

    private static Method channelMethod(String name, Class<?>... args) {
        try {
            return Channel.class.getDeclaredMethod(name, args);
        } catch (Exception ex) {
            throw new RuntimeException("Could not get channel method: " + name, ex);
        }
    }
}
