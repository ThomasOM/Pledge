package dev.thomazz.pledge.api;

/**
 * Policy for packets when being written to the player netty channel.
 */
public enum PacketWritePolicy {
    WRITE_FLUSH, // Default Minecraft packet write policy, always flushing on each packet
    WRITE // Some server forks use an optimized writing policy that require this
}
