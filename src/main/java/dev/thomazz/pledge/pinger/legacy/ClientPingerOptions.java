package dev.thomazz.pledge.pinger.legacy;

import dev.thomazz.pledge.pinger.PingerOptions;
import lombok.Getter;

/**
 * Extra options for creating a {@link ClientPinger}
 * <p>
 * {@link ClientPingerOptions#consolidatePackets} - Asynchronously sent packets are queued and processed when pinging
 */
@Getter
public class ClientPingerOptions extends PingerOptions {
    private boolean consolidatePackets;

    public static ClientPingerOptions range(int startId, int endId) {
        return ClientPingerOptions.range(startId, endId, true);
    }

    public static ClientPingerOptions range(int startId, int endId, boolean consolidatePackets) {
        ClientPingerOptions options = new ClientPingerOptions();
        options.startId = startId;
        options.endId = endId;
        options.consolidatePackets = consolidatePackets;
        return options;
    }
}
