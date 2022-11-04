package dev.thomazz.pledge.api;

import lombok.Data;
import lombok.ToString;

/**
 * Represents a pair of signaling packets that wraps all packets sent during a server tick.
 */
@Data
@ToString
public class PacketFrame {
    private final int id1;
    private final int id2;
}
