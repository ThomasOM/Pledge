package dev.thomazz.pledge.transaction;

import dev.thomazz.pledge.api.event.TransactionEvent;
import dev.thomazz.pledge.api.event.TransactionListener;

public enum TransactionEventType {
    SEND_START(TransactionListener::onSendStart),
    SEND_END(TransactionListener::onSendEnd),
    RECEIVE_START(TransactionListener::onReceiveStart),
    RECEIVE_END(TransactionListener::onReceiveEnd),
    ERROR(TransactionListener::onError);

    private final EventProcess process;

    TransactionEventType(EventProcess process) {
        this.process = process;
    }

    public void processEvent(TransactionListener listener, TransactionEvent event) {
        this.process.processEvent(listener, event);
    }

    private interface EventProcess {
        void processEvent(TransactionListener listener, TransactionEvent event);
    }
}
