package dev.thomazz.pledge.pinger;

import lombok.Builder;
import lombok.Data;

/**
 * Options for creating a {@link ClientPinger}
 * <p>
 * {@link ClientPingerOptions#startId} - Start ID for ping range
 * {@link ClientPingerOptions#endId} - End ID for ping range
 * {@link ClientPingerOptions#consolidatePackets} - Asynchronously sent packets are queued and processed when pinging
 */
@Data
@Builder
public final class ClientPingerOptions {
    @Builder.Default private int startId = -1000;
    @Builder.Default private int endId = -2000;
    @Builder.Default private boolean consolidatePackets = true;

    public static ClientPingerOptions range(int startId, int endId) {
        return ClientPingerOptions.builder().startId(startId).endId(endId).build();
    }
}
