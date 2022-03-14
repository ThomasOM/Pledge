package dev.thomazz.pledge.util;

import io.netty.channel.Channel;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public final class MinecraftUtil {
    private static final String BASE = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS = MinecraftUtil.BASE.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    // Some caching to speed up reflection
    private static Method PLAYER_GET_HANDLE;
    private static Method SERVER_GET_METHOD;
    private static Field CONNECTION_FIELD;
    private static Field NETWORK_MANAGER_FIELD;
    private static Field CHANNEL_FIELD;

    public static Class<?> legacyNms(String className) throws ClassNotFoundException {
        return Class.forName(MinecraftUtil.NMS + "." + className);
    }

    public static Class<?> gamePacket(String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.network.protocol.game." + className);
    }

    public static Object getMinecraftServer() throws Exception {
        Server server = Bukkit.getServer();

        // Internal server
        if (MinecraftUtil.SERVER_GET_METHOD == null) {
            MinecraftUtil.SERVER_GET_METHOD = server.getClass().getDeclaredMethod("getServer");
        }

        return MinecraftUtil.SERVER_GET_METHOD.invoke(server);
    }

    public static Channel getChannelFromPlayer(Player player) throws Exception {
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

        Object networkManager = MinecraftUtil.NETWORK_MANAGER_FIELD.get(connection);

        // Channel
        if (MinecraftUtil.CHANNEL_FIELD == null) {
            MinecraftUtil.CHANNEL_FIELD = ReflectionUtil.getFieldByType(networkManager.getClass(), Channel.class);
        }

        return (Channel) MinecraftUtil.CHANNEL_FIELD.get(networkManager);
    }

    public static Object getPlayerConnection(Player player) throws Exception {
        // Player Handle
        if (MinecraftUtil.PLAYER_GET_HANDLE == null) {
            MinecraftUtil.PLAYER_GET_HANDLE = player.getClass().getDeclaredMethod("getHandle");
        }

        Object handle = MinecraftUtil.PLAYER_GET_HANDLE.invoke(player);

        // Player Connection
        if (MinecraftUtil.CONNECTION_FIELD == null) {
            MinecraftUtil.CONNECTION_FIELD = ReflectionUtil.getFieldByClassNames(handle.getClass(), "PlayerConnection", "b");
        }

        return MinecraftUtil.CONNECTION_FIELD.get(handle);
    }
}
