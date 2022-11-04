package dev.thomazz.pledge.api.event;

/**
 * Type of event used in {@link PacketFrameReceiveEvent}
 */
public enum ReceiveType {
    RECEIVE_START, // When the first packet of the frame is received
    RECEIVE_END // When the second packet of the frame is received, completing the frame
}
