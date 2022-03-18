package dev.thomazz.pledge.api.event;

/**
 * Listener for {@link TransactionEvent}s.
 *
 * All methods here are called from the netty event loop,
 * anything intensive should be moved to a different thread.
 */
public interface TransactionListener {
    /**
     * Called when the server sends the first transaction of an {@link ActionPair}.
     */
    default void onSendStart(TransactionEvent event) {
    }

    /**
     * Called when the server has sent both transactions of an {@link ActionPair}.
     */
    default void onSendEnd(TransactionEvent event) {
    }

    /**
     * Called when the server received the first transaction of an {@link ActionPair}.
     */
    default void onReceiveStart(TransactionEvent event) {
    }

    /**
     * Called when the server received both transactions of an {@link ActionPair}.
     */
    default void onReceiveEnd(TransactionEvent event) {
    }

    /**
     * Called whenever an error occurs when receiving a transaction within the specified range.
     * These errors happen when a transaction is received and the action number does not match the expected value.
     * In most cases you can kick players when this occurs.
     */
    default void onError(TransactionEvent event) {
    }
}
