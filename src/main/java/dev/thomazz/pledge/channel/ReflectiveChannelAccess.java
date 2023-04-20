package dev.thomazz.pledge.channel;

import dev.thomazz.pledge.util.MinecraftReflection;
import dev.thomazz.pledge.util.ReflectionUtil;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@SuppressWarnings("unchecked")
public final class ReflectiveChannelAccess implements ChannelAccess {
    @Override
    public Channel getChannel(Player player) {
        try {
            Object handle = player.getClass().getDeclaredMethod("getHandle").invoke(player);
            Field connectionField = ReflectionUtil.getFieldByClassNames(handle.getClass(), "PlayerConnection", "b");
            Object connection = connectionField.get(handle);
            Field networkManagerField = ReflectionUtil.getFieldByClassNames(connection.getClass(), "NetworkManager");
            Object networkManager = networkManagerField.get(connection);
            Field channelField = ReflectionUtil.getFieldByType(networkManager.getClass(), Channel.class);
            return (Channel) channelField.get(networkManager);
        } catch (Exception ex) {
            throw new RuntimeException("Could not get channel for player: " + player.getName(), ex);
        }
    }

    @Override
    public Channel getChannel(InetAddress address) {
        return this.getAllChannels().stream()
                .filter(channel -> ((InetSocketAddress) channel.remoteAddress()).getAddress().equals(address))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No channel linked to address: " + address));
    }

    @Override
    public Collection<Channel> getAllChannels() {
        try {
            Object serverConnection = MinecraftReflection.getServerConnection();
            for (Field field : serverConnection.getClass().getDeclaredFields()) {
                if (!List.class.isAssignableFrom(field.getType()) || !field.getGenericType().getTypeName().contains("NetworkManager")) {
                    continue;
                }

                field.setAccessible(true);

                List<Channel> channels = new ArrayList<>();
                List<Object> networkManagers = (List<Object>) field.get(serverConnection);

                Class<?> listType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                Field channelField = ReflectionUtil.getFieldByType(listType, Channel.class);

                for (Object o : Collections.synchronizedList(networkManagers)) {
                    Channel channel = (Channel) channelField.get(o);
                    channels.add(channel);
                }

                return channels;
            }

            throw new NoSuchElementException("Did not find correct list in server connection");
        } catch (Exception ex) {
            throw new RuntimeException("Cannot access server channels", ex);
        }
    }
}
