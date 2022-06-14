package dev.thomazz.pledge.transaction;

import dev.thomazz.pledge.api.Direction;
import dev.thomazz.pledge.api.HandlerInfo;
import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.api.event.TransactionEvent;
import dev.thomazz.pledge.util.MinecraftUtil;
import dev.thomazz.pledge.util.PacketUtil;
import dev.thomazz.pledge.transaction.recycle.RecyclableTransaction;
import dev.thomazz.pledge.transaction.recycle.TransactionRecycler;
import io.netty.channel.Channel;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.bukkit.entity.Player;

public class TransactionHandler implements HandlerInfo {
    private final Reference<Player> playerReference;
    private final Reference<Channel> channelReference;
    private final Reference<Object> connectionReference; // For quick access

    private final IntObjectMap<TransactionPair> indexMapping = new IntObjectHashMap<>();

    private final Direction direction;
    private final int min;
    private final int max;

    private int index;
    private int expectedIndex;

    private TransactionPair receivingPair;
    private TransactionPair sendingPair;

    TransactionHandler(Player player, Channel channel, Direction direction, int min, int max) throws Exception {
        // We don't want player objects and channels to persist because of these references
        this.playerReference = new WeakReference<>(player);
        this.channelReference = new WeakReference<>(channel);
        this.connectionReference = new WeakReference<>(MinecraftUtil.getPlayerConnection(player));

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

    public boolean tickStart() {
        this.onEventLoop(() -> {
            this.sendingPair = new TransactionPair(this.index);
            this.addIndex(this.index, this.sendingPair);

            // Write a transaction packet to the channel and call send start
            this.writeTransaction(this.index);
            this.callEvent(TransactionEventType.SEND_START, this.sendingPair);

            this.index = this.updateIndex(this.index);
        });

        return this.isClosed();
    }

    public boolean tickEnd() {
        this.onEventLoop(() -> {
            if (this.sendingPair != null) {
                this.sendingPair.setId2(this.index);

                // Write a transaction packet to the channel and call send end
                this.writeTransaction(this.index);
                this.callEvent(TransactionEventType.SEND_END, this.sendingPair);

                this.index = this.updateIndex(this.index);
            }
        });

        return this.isClosed();
    }

    public void handleIncomingTransaction(int id) {
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
                this.receivingPair = this.fromIndex(id);

                // Throw error if ID is not the expected index
                if (id != this.expectedIndex) {
                    this.callEvent(TransactionEventType.ERROR, this.receivingPair);
                }

                // Call receive start event
                this.callEvent(TransactionEventType.RECEIVE_START, this.receivingPair);
            }
        }
    }

    private boolean isClosed() {
        Channel channel = this.getChannel();
        return channel == null || !channel.isOpen();
    }

    private void writeTransaction(int index) {
        try {
            TransactionRecycler recycler = PledgeImpl.INSTANCE.getRecycler();
            RecyclableTransaction recyclable = recycler.get();
            recyclable.setupIndex(index);

            // We can't write recyclables directly to the channel, use the raw packet
            PacketUtil.sendPacket(this.connectionReference.get(), recyclable.getRawPacket());

            // We can recycle after this since this is on the event loop
            recyclable.recycle();
        } catch (Throwable throwable) {
            PledgeImpl.LOGGER.severe("Something went wrong sending a transaction packet!");
            throwable.printStackTrace();
        }
    }

    private void callEvent(TransactionEventType type, TransactionPair pair) {
        if (PledgeImpl.INSTANCE.hasEvents()) {
            TransactionEvent event = new TransactionEvent(this, pair);
            PledgeImpl.INSTANCE.getTransactionManager().callEvent(type, event);
        }
    }

    private void onEventLoop(Runnable runnable) {
        Channel channel = this.getChannel();
        if (channel != null && channel.isOpen()) {
            channel.eventLoop().execute(runnable);
        }
    }

    private void addIndex(int index, TransactionPair pair) {
        index = this.convertIndex(index);
        synchronized (this.indexMapping) {
            this.indexMapping.put(index, pair);
        }
    }

    private TransactionPair fromIndex(int index) {
        index = this.convertIndex(index);
        synchronized (this.indexMapping) {
            return this.indexMapping.get(index);
        }
    }

    private int convertIndex(int index) {
        return Math.abs(index); // Right now we only support negative ids
    }

    private int updateIndex(int index) {
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
