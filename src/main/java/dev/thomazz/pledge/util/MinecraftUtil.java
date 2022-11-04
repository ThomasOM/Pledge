package dev.thomazz.pledge.util;

import io.netty.channel.Channel;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@UtilityClass
public class MinecraftUtil {
    private final String BASE = Bukkit.getServer().getClass().getPackage().getName();
    private final String NMS = MinecraftUtil.BASE.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    // Some caching to speed up reflection
    private Method PLAYER_GET_HANDLE;
    private Field CONNECTION_FIELD;
    private Field NETWORK_MANAGER_FIELD;
    private Field CHANNEL_FIELD;

    public Class<?> legacyNms(String className) throws Exception {
        return Class.forName(MinecraftUtil.NMS + "." + className);
    }

    public Class<?> gamePacket(String className) throws Exception {
        // Support for legacy versions
        try {
            return MinecraftUtil.legacyNms(className);
        } catch (Exception ignored) {
        }

        // Otherwise try the game packet class
        return Class.forName("net.minecraft.network.protocol.game." + className);
    }

    public Channel getChannel(Player player) throws Exception {
        Object networkManager = MinecraftUtil.getNetworkManager(player);

        // Channel
        if (MinecraftUtil.CHANNEL_FIELD == null) {
            MinecraftUtil.CHANNEL_FIELD = ReflectionUtil.getFieldByType(networkManager.getClass(), Channel.class);
        }

        return (Channel) MinecraftUtil.CHANNEL_FIELD.get(networkManager);
    }

    public void setChannel(Player player, Channel channel) throws Exception {
        Object networkManager = MinecraftUtil.getNetworkManager(player);

        // Channel
        if (MinecraftUtil.CHANNEL_FIELD == null) {
            MinecraftUtil.CHANNEL_FIELD = ReflectionUtil.getFieldByType(networkManager.getClass(), Channel.class);
        }

        MinecraftUtil.CHANNEL_FIELD.set(networkManager, channel);
    }

    private Object getNetworkManager(Player player) throws Exception {
        // Player Handle
        if (MinecraftUtil.PLAYER_GET_HANDLE == null) {
            MinecraftUtil.PLAYER_GET_HANDLE = player.getClass().getDeclaredMethod("getHandle");
        }

        Object handle = MinecraftUtil.PLAYER_GET_HANDLE.invoke(player);

        // Player Connection
        if (MinecraftUtil.CONNECTION_FIELD == null) {
            MinecraftUtil.CONNECTION_FIELD = ReflectionUtil.getFieldByClassNames(handle.getClass(), "PlayerConnection", "b");
        }

        Object connection = MinecraftUtil.CONNECTION_FIELD.get(handle);

        // Network Manager
        if (MinecraftUtil.NETWORK_MANAGER_FIELD == null) {
            MinecraftUtil.NETWORK_MANAGER_FIELD = ReflectionUtil.getFieldByClassNames(connection.getClass(), "NetworkManager");
        }

        return MinecraftUtil.NETWORK_MANAGER_FIELD.get(connection);
    }
}
