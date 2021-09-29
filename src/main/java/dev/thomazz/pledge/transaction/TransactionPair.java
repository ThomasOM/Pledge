package dev.thomazz.pledge.transaction;

import dev.thomazz.pledge.api.event.ActionPair;

public class TransactionPair implements ActionPair {
    private final int id1;
    private int id2;

    TransactionPair(int id1) {
        this.id1 = id1;
    }

    void setId2(int id2) {
        this.id2 = id2;
    }

    public int getId1() {
        return this.id1;
    }

    public int getId2() {
        return this.id2;
    }
}
