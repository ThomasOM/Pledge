package dev.thomazz.pledge.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a pair of signaling packets that wraps all packets sent during a server tick.
 */
@Getter
@RequiredArgsConstructor
public class PacketFrame {
	private final int id1;
	private final int id2;
}
