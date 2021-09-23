package dev.thomazz.pledge.api.event;

import dev.thomazz.pledge.api.HandlerInfo;
import org.bukkit.entity.Player;
import io.netty.channel.Channel;

/**
 * Transaction event containing {@link HandlerInfo} and {@link ActionPair}.
 */
public class TransactionEvent {
    private final HandlerInfo info;
    private final ActionPair actionPair;

    public TransactionEvent(HandlerInfo info, ActionPair pair) {
        this.info = info;
        this.actionPair = pair;
    }

    /**
     * Provides {@link HandlerInfo} which contains methods for getting the {@link Player} and the {@link Channel}.
     *
     * @return - Handler info
     */
    public HandlerInfo getInfo() {
        return this.info;
    }

    /**
     * Gets the transaction pair the event is associated with.
     *
     * @return - Transaction pair
     */
    public ActionPair getTransactionPair() {
        return this.actionPair;
    }
}
