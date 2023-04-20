package dev.thomazz.pledge.channel;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.util.Collection;

public interface ChannelAccess {
    Channel getChannel(Player player);

    Channel getChannel(InetAddress address);

    Collection<Channel> getAllChannels();
}
