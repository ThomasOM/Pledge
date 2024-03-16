package dev.thomazz.pledge.pinger.frame.data;

import lombok.Data;

/**
 * Object containing ping packet IDs linked to the start and end of a tick.
 */
@Data
public class Frame {
    private final int startId;
    private final int endId;
}
