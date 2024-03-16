package dev.thomazz.pledge.util;

import io.netty.channel.Channel;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@UtilityClass
public final class ChannelAccess {
    private final Class<?> NETWORK_MANAGER_CLASS = MinecraftReflection.getMinecraftClass(
        "network.NetworkManager",
        "NetworkManager"
    );

    private final Class<?> PACKET_LISTENER_CLASS = MinecraftReflection.getMinecraftClass(
        "network.PacketListener",
        "PacketListener"
    );

    private final Class<?> PLAYER_CONNECTION_CLASS = MinecraftReflection.getMinecraftClass(
        "server.network.PlayerConnection",
        "PlayerConnection"
    );

    public Channel getChannel(Player player) {
        try {
            UUID playerId = player.getUniqueId();
            Object handle = player.getClass().getDeclaredMethod("getHandle").invoke(player);

            Field playerConnectionField = ReflectionUtil.getFieldByType(handle.getClass(), ChannelAccess.PLAYER_CONNECTION_CLASS);
            Field networkManagerField = ReflectionUtil.getFieldByType(ChannelAccess.PLAYER_CONNECTION_CLASS, ChannelAccess.NETWORK_MANAGER_CLASS);
            Field channelField = ReflectionUtil.getFieldByType(ChannelAccess.NETWORK_MANAGER_CLASS, Channel.class);

            // Try the easy way first
            Object playerConnection = playerConnectionField.get(handle);
            if (playerConnection != null) {
                Object networkManager = networkManagerField.get(playerConnection);
                return (Channel) channelField.get(networkManager);
            }

            // Try to match all network managers after from game profile
            List<Object> networkManagers = ChannelAccess.getNetworkManagers();
            Field listenerField = ReflectionUtil.getFieldByType(ChannelAccess.NETWORK_MANAGER_CLASS, ChannelAccess.PACKET_LISTENER_CLASS);

            for (Object networkManager : networkManagers) {
                Object packetListener = listenerField.get(networkManager);
                if (packetListener != null) {
                    if (packetListener.getClass().getSimpleName().equals("LoginListener")) {
                        Field profileField = ReflectionUtil.getFieldByClassNames(packetListener.getClass(), "GameProfile");
                        Object gameProfile = profileField.get(packetListener);

                        // We can use the game profile to look up the player id in the listener
                        Field uuidField = ReflectionUtil.getFieldByType(gameProfile.getClass(), UUID.class);
                        UUID foundId = (UUID) uuidField.get(gameProfile);
                        if (playerId.equals(foundId)) {
                            return (Channel) channelField.get(networkManager);
                        }
                    } else {
                        // For player connection listeners we can get the player handle
                        Field playerField;
                        try {
                            playerField = ReflectionUtil.getFieldByClassNames(packetListener.getClass(), "EntityPlayer");
                        } catch (NoSuchFieldException ignored) {
                            // Might be ServerConfigurationPacketListenerImpl or something else that is unsupported
                            continue;
                        }

                        Object entityPlayer = playerField.get(packetListener);
                        if (handle.equals(entityPlayer)) {
                            return (Channel) channelField.get(networkManager);
                        }
                    }
                }
            }

            throw new NoSuchElementException("Did not find player channel!");
        } catch (Exception ex) {
            throw new RuntimeException("Could not get channel for player: " + player.getName(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> getNetworkManagers() {
        try {
            Object serverConnection = MinecraftReflection.getServerConnection();
            for (Field field : serverConnection.getClass().getDeclaredFields()) {
                if (!List.class.isAssignableFrom(field.getType()) || !field.getGenericType().getTypeName().contains("NetworkManager")) {
                    continue;
                }

                field.setAccessible(true);

                List<Object> networkManagers = (List<Object>) field.get(serverConnection);
                return Collections.synchronizedList(networkManagers);
            }

            throw new NoSuchElementException("Did not find correct list in server connection");
        } catch (Exception ex) {
            throw new RuntimeException("Cannot retrieve network managers", ex);
        }
    }
}
