package dev.thomazz.pledge.transaction;

import dev.thomazz.pledge.api.Direction;
import dev.thomazz.pledge.api.HandlerInfo;
import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.api.event.TransactionEvent;
import dev.thomazz.pledge.util.MinecraftUtil;
import dev.thomazz.pledge.util.PacketUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class TransactionHandler implements HandlerInfo {
    private final Reference<Channel> channelReference;
    private final Direction direction;
    private final short min;
    private final short max;

    private short index;
    private short expectedIndex;

    private Map<Short, TransactionPair> receivingPairMapping = new HashMap<>();
    private TransactionPair receivingPair;
    private TransactionPair sendingPair;

    // Could be null when player hasn't been created yet
    private volatile Reference<Player> playerReference;

    TransactionHandler(Channel channel, Direction direction, short min, short max) {
        // We need to be careful with netty and don't want channels to persist because of this reference
        this.channelReference = new WeakReference<>(channel);

        this.direction = Direction.NEGATIVE;
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
        if (this.playerReference != null) {
            return this.playerReference.get();
        }

        // Look up player through the network manager
        Player lookup = null;

        Channel channel = this.getChannel();
        if (channel != null) {
            ChannelPipeline pipeline = channel.pipeline();
            Object networkManager = pipeline.get("packet_handler");

            if (networkManager != null) {
                try {
                    lookup = MinecraftUtil.getPlayerFromManager(networkManager);

                    // Create a weak reference because we don't want to interrupt server memory management
                    if (lookup != null) {
                        this.playerReference = new WeakReference<>(lookup);
                    }
                } catch (Exception e) {
                    PledgeImpl.LOGGER.severe("Something went wrong retrieving the player from a channel!");
                    e.printStackTrace();
                }
            }
        }

        return lookup;
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
        if (this.isOpen()) {
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
            case POSITIVE:
                index--;

                if (index <= this.min) {
                    index = this.max;
                }
                break;
            case NEGATIVE:
                index++;

                if (index >= this.max) {
                    index = this.min;
                }
                break;
        }

        return index;
    }
}
