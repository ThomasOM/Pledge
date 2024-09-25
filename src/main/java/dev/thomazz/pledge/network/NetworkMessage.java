package dev.thomazz.pledge.network;

import io.netty.channel.ChannelPromise;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class NetworkMessage {
    private final Object message;
    private final ChannelPromise promise;
}
