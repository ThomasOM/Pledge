package dev.thomazz.pledge.api;

import java.util.Queue;

/**
 * Examines the packets sent in a server tick and determines if they should be tracked using a {@link PacketFrame}
 */
public interface PacketTickExaminer {
	/**
	 * Determines if the packets in the current server tick should be tracked using a {@link PacketFrame}
	 *
	 * @param packets - Queue of raw packet objects sent from server to player
	 * @return        - If the packets should be tracked with a {@link PacketFrame}
	 */
	boolean shouldTrack(Queue<Object> packets);
}
