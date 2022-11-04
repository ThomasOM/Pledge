package dev.thomazz.pledge.api.event;

/**
 * Error types used in {@link PacketFrameErrorEvent}
 */
public enum ErrorType {
    MISSING_FRAME, // Whenever a packet within the range does not have a frame sent
    INCOMPLETE_FRAME // Whenever the currently received frame is not completed with the correct id
}
