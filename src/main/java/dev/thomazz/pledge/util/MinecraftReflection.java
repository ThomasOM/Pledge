package dev.thomazz.pledge.util;

import java.lang.reflect.Field;
import java.util.Arrays;

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
        try {
            return Class.forName("net.minecraft.network.protocol.game." + className);
        } catch (Exception ignored) {}

        // Otherwise try common packet class for 1.20.2+
        return Class.forName("net.minecraft.network.protocol.common." + className);
    }

    public Class<?> getMinecraftClass(String... names) {
        String[] packageNames = new String[] {
            MinecraftReflection.getMinecraftPackage(),
            MinecraftReflection.getMinecraftPackageLegacy()
        };

        for (String packageName : packageNames) {
            for(String name : names) {
                try {
                    return Class.forName(packageName + "." + name);
                } catch (Throwable ignored) {
                }
            }
        }

        throw new RuntimeException("Could not find minecraft class: " + Arrays.toString(names));
    }

    public String getCraftBukkitPackage() {
        return Bukkit.getServer().getClass().getPackage().getName();
    }

    public String getMinecraftPackage() {
        return "net.minecraft";
    }

    public String getMinecraftPackageLegacy() {
        return MinecraftReflection.getCraftBukkitPackage()
            .replace("org.bukkit.craftbukkit", "net.minecraft.server");
    }

    public Object getServerConnection() throws Exception {
        Object minecraftServer = Bukkit.getServer().getClass().getDeclaredMethod("getServer").invoke(Bukkit.getServer());
        Field connectionField = ReflectionUtil.getFieldByClassNames(minecraftServer.getClass().getSuperclass(), "ServerConnection");
        return connectionField.get(minecraftServer);
    }
}
