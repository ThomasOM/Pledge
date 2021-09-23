package dev.thomazz.pledge.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class MinecraftUtil {
    private static String BASE = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS = MinecraftUtil.BASE.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    public static Class<?> nms(String className) throws ClassNotFoundException {
        return Class.forName(MinecraftUtil.NMS + "." + className);
    }

    // Don't really care if this is slow, because the object gets cached after retrieving it
    public static Player getPlayerFromManager(Object networkManager) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        for (Field field : networkManager.getClass().getDeclaredFields()) {
            if (field.getType().getSimpleName().equalsIgnoreCase("PacketListener")) {
                field.setAccessible(true);
                Object listener = field.get(networkManager);
                if (listener.getClass().getSimpleName().equalsIgnoreCase("PlayerConnection")) {
                    for (Field inner : listener.getClass().getDeclaredFields()) {
                        if (inner.getType().getSimpleName().equalsIgnoreCase("EntityPlayer") || inner.getType().getSimpleName().equalsIgnoreCase("Player")) {
                            Object entityPlayer = inner.get(listener);
                            Method getBukkitEntity = entityPlayer.getClass().getDeclaredMethod("getBukkitEntity");
                            return (Player) getBukkitEntity.invoke(entityPlayer);
                        }
                    }
                    break;
                }
            }
        }

        return null;
    }

    // Only used on startup
    public static Object getServerConnection() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Class<?> serverClazz = MinecraftUtil.nms("MinecraftServer");
        Object server = ReflectionUtil.invokeStatic(serverClazz, "getServer");
        Object connection = null;

        for (Method m : serverClazz.getDeclaredMethods()) {
            if (m.getReturnType() != null) {
                if (m.getReturnType().getSimpleName().equals("ServerConnection")) {
                    if (m.getParameterTypes().length == 0) {
                        connection = m.invoke(server);
                    }
                }
            }
        }

        return connection;
    }
}
