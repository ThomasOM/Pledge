package dev.thomazz.pledge.inject.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import java.lang.reflect.Method;

public abstract class EnclosingChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final ChannelInitializer<?> original;
    private final Method method;

    public EnclosingChannelInitializer(ChannelInitializer<?> initializer) {
        this.original = initializer;

        try {
            this.method = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            this.method.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("Method 'initChannel' could not be found in class 'ChannelInitializer'... What?");
        }
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        this.method.invoke(this.original, channel);
        this.onInitChannel(channel);
    }

    public abstract void onInitChannel(SocketChannel channel);

    public ChannelInitializer<?> getOriginal() {
        return this.original;
    }
}
