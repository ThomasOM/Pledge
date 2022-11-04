package dev.thomazz.pledge.network.delegation;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class WriteFlushInvocationHandler implements InvocationHandler {
    private final Channel original;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
       if (method.getName().equalsIgnoreCase("writeAndFlush")) {
           if (args.length > 1) {
               return this.original.write(args[0], (ChannelPromise) args[1]);
           } else {
               return this.original.write(args[0]);
           }
       } else {
           return method.invoke(this.original, args);
       }
    }
}
