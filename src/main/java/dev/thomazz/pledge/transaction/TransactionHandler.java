package dev.thomazz.pledge.transaction;

import dev.thomazz.pledge.api.Direction;
import dev.thomazz.pledge.api.HandlerInfo;
import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.api.event.TransactionEvent;
import dev.thomazz.pledge.util.PacketUtil;
import io.netty.channel.Channel;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class TransactionHandler implements HandlerInfo {
    private final Reference<Player> playerReference;
    private final Reference<Channel> channelReference;
    private final Direction direction;
    private final short min;
    private final short max;

    private short index;
    private short expectedIndex;

    private Map<Short, TransactionPair> receivingPairMapping = new HashMap<>();
    private TransactionPair receivingPair;
    private TransactionPair sendingPair;

    TransactionHandler(Player player, Channel channel, Direction direction, short min, short max) throws Exception {
        // We don't want player objects and channels to persist because of this reference
        this.playerReference = new WeakReference<>(player);
        this.channelReference = new WeakReference<>(channel);

        this.direction = direction;
        this.min = min;
        this.max = max;

        this.index = direction == Direction.POSITIVE ? min : max;
        this.expectedIndex = this.index;
    }

    @Override
    public Channel getChannel() {
        return this.channelReference.get();
    }

    @Override
    public Player getPlayer() {
        return this.playerReference.get();
    }

    public void tickStart() {
        if (this.isOpen()) {
            this.sendingPair = new TransactionPair(this.index);
            this.receivingPairMapping.put(this.index, this.sendingPair);

            // Write a transaction packet to the channel and call send start
            this.getChannel().writeAndFlush(PacketUtil.buildTransactionPacket(this.index));
            this.callEvent(TransactionEventType.SEND_START, this.sendingPair);

            this.index = this.updateIndex(this.index);
        }
    }

    public void tickEnd() {
        if (this.isOpen() && this.sendingPair != null) {
            this.sendingPair.setId2(this.index);

            // Write a transaction packet to the channel and call send end
            this.getChannel().writeAndFlush(PacketUtil.buildTransactionPacket(this.index));
            this.callEvent(TransactionEventType.SEND_END, this.sendingPair);

            this.index = this.updateIndex(this.index);
        }
    }

    public void handleIncomingTransaction(short id) {
        // First check if the ID is within bounds
        if (id >= this.min && id <= this.max) {
            if (this.receivingPair != null) {
                // If the secondary ID doesn't match the second receiving pair ID, an error occurs
                if (id == this.receivingPair.getId2()) {
                    this.callEvent(TransactionEventType.RECEIVE_END, this.receivingPair);
                } else {
                    this.callEvent(TransactionEventType.ERROR, this.receivingPair);
                }

                // Set new expected index and clear finished receiving pair
                this.expectedIndex = this.updateIndex(this.receivingPair.getId2());
                this.receivingPair = null;
            } else {
                // Locate receiving pair
                this.receivingPair = this.receivingPairMapping.remove(id);

                // Throw error if ID is not the expected index
                if (id != this.expectedIndex) {
                    this.callEvent(TransactionEventType.ERROR, this.receivingPair);
                }

                // Call receive start event
                this.callEvent(TransactionEventType.RECEIVE_START, this.receivingPair);
            }
        }
    }

    public boolean isOpen() {
        Channel channel = this.getChannel();
        return channel != null && channel.isOpen();
    }

    private void callEvent(TransactionEventType type, TransactionPair pair) {
        if (PledgeImpl.INSTANCE.hasEvents()) {
            TransactionEvent event = new TransactionEvent(this, pair);
            PledgeImpl.INSTANCE.getTransactionManager().callEvent(type, event);
        }
    }

    private short updateIndex(short index) {
        // Depending on the direction, we count until we reach the boundary and flip to the opposite boundary
        switch (this.direction) {
            case NEGATIVE:
                if (index <= this.min) {
                    index = this.max;
                } else {
                    index--;
                }
                break;
            case POSITIVE:
                if (index >= this.max) {
                    index = this.min;
                } else {
                    index++;
                }
                break;
        }

        return index;
    }
}
