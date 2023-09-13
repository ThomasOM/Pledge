package dev.thomazz.pledge.network;

import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class QueuedMessage {
    private final Object packet;
    private final ChannelPromise promise;
}