package dev.thomazz.pledge.transaction;

import dev.thomazz.pledge.api.event.ActionPair;

public class TransactionPair implements ActionPair {
    private final short id1;
    private short id2;

    TransactionPair(short id1) {
        this.id1 = id1;
    }

    void setId2(short id2) {
        this.id2 = id2;
    }

    public short getId1() {
        return this.id1;
    }

    public short getId2() {
        return this.id2;
    }
}
