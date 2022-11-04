package dev.thomazz.pledge.network.delegation;

import io.netty.channel.Channel;
import java.lang.reflect.Proxy;

public final class DelegateChannelFactory {
    public static Channel buildDelegateChannel(Channel delegate) {
        WriteFlushInvocationHandler handler = new WriteFlushInvocationHandler(delegate);
        return (Channel) Proxy.newProxyInstance(Channel.class.getClassLoader(), new Class[]{Channel.class}, handler);
    }
}
