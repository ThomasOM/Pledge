package dev.thomazz.pledge.api.event;

/**
 * Represents a pair of transactions that wraps all packets sent during a server tick.
 */
public interface ActionPair {

    /**
     * @return The action number of the first transaction of this pair.
     */
    short getId1();

    /**
     * @return The action number of the second transaction of this pair.
     */
    short getId2();
}
