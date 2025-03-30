package dev.thomazz.pledge.util;

import io.netty.channel.Channel;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

@UtilityClass
public class ChannelUtils {
    public void runInEventLoop(Channel channel, Runnable runnable) {
        if (!channel.eventLoop().inEventLoop()) {
            channel.eventLoop().execute(runnable);
        } else {
            runnable.run();
        }
    }

    public boolean ensureInEventLoop(Channel channel, Runnable runnable) {
        if (!channel.eventLoop().inEventLoop()) {
            channel.eventLoop().execute(runnable);
            return false;
        }

        return true;
    }
}
