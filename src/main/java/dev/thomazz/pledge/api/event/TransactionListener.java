package dev.thomazz.pledge.api.event;

/**
 * Listener for {@link TransactionEvent}s.
 */
public interface TransactionListener {

    /**
     * Called when the server sends the first transaction of an {@link ActionPair}.
     * Note: Called from the server main thread, please take care.
     */
    default void onSendStart(TransactionEvent event) {
        // Optional implementation
    }

    /**
     * Called when the server has sent both transaction of an {@link ActionPair}.
     * Note: Called from the server main thread, please take care.
     */
    default void onSendEnd(TransactionEvent event) {
        // Optional implementation
    }

    /**
     * Called when the server received the first transaction of an {@link ActionPair}.
     * Note: Called from a netty thread, anything intensive should not be done on this thread.
     */
    default void onReceiveStart(TransactionEvent event) {
        // Optional implementation
    }

    /**
     * Called when the server received both transactions of an {@link ActionPair}.
     * Note: Called from a netty thread, anything intensive should not be done on this thread.
     */
    default void onReceiveEnd(TransactionEvent event) {
        // Optional implementation
    }

    /**
     * Called whenever an error occurs when receiving a transaction within the specified range.
     * These errors happen when a transaction is received and the action number does not match the expected value.
     */
    default void onError(TransactionEvent event) {
        // Optional implementation
    }
}
