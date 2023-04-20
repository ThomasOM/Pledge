package dev.thomazz.pledge.util;

import java.lang.reflect.Field;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class MinecraftReflection {
    private final String BASE = Bukkit.getServer().getClass().getPackage().getName();
    private final String NMS = MinecraftReflection.BASE.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    public Class<?> legacyNms(String className) throws Exception {
        return Class.forName(MinecraftReflection.NMS + "." + className);
    }

    public Class<?> gamePacket(String className) throws Exception {
        // Support for legacy versions
        try {
            return MinecraftReflection.legacyNms(className);
        } catch (Exception ignored) {
        }

        // Otherwise try the game packet class
        return Class.forName("net.minecraft.network.protocol.game." + className);
    }

    public Object getServerConnection() throws Exception {
        Object minecraftServer = Bukkit.getServer().getClass().getDeclaredMethod("getServer").invoke(Bukkit.getServer());
        Field connectionField = ReflectionUtil.getFieldByClassNames(minecraftServer.getClass().getSuperclass(), "ServerConnection");
        return connectionField.get(minecraftServer);
    }
}
