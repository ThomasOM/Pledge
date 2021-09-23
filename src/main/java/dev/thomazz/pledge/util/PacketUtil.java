package dev.thomazz.pledge.util;

import dev.thomazz.pledge.PledgeImpl;
import java.lang.reflect.Field;

public final class PacketUtil {
    // Cache reflection stuff to make this faster
    public static Class<?> TRANSACTION_CLASS;
    public static Field WINDOW_FIELD;
    public static Field ACCEPT_FIELD;
    public static Field ACTION_FIELD;

    // Defaults that are used
    private static int TRANSACTION_WINDOW_ID = 0;
    private static boolean TRANSACTION_ACCEPT_STATE = false;

    static {
        try {
            PacketUtil.TRANSACTION_CLASS = MinecraftUtil.nms("PacketPlayOutTransaction");
            PacketUtil.WINDOW_FIELD = ReflectionUtil.get(PacketUtil.TRANSACTION_CLASS, int.class, 0);
            PacketUtil.ACCEPT_FIELD = ReflectionUtil.get(PacketUtil.TRANSACTION_CLASS, short.class, 0);
            PacketUtil.ACTION_FIELD = ReflectionUtil.get(PacketUtil.TRANSACTION_CLASS, int.class, 0);
        } catch (Exception e) {
            PledgeImpl.LOGGER.severe("Could not initialize transaction values!");
            e.printStackTrace();
        }
    }

    public static Object buildTransactionPacket(short actionNumber) {
        try {
            Object packet = PacketUtil.TRANSACTION_CLASS.newInstance();

            PacketUtil.WINDOW_FIELD.set(packet, PacketUtil.TRANSACTION_WINDOW_ID);
            PacketUtil.ACCEPT_FIELD.set(packet, PacketUtil.TRANSACTION_ACCEPT_STATE);
            PacketUtil.ACTION_FIELD.set(packet, actionNumber);

            return packet;
        } catch (IllegalAccessException | InstantiationException e) {
            PledgeImpl.LOGGER.severe("Could not build transaction packet!");
            e.printStackTrace();
        }

        return null;
    }

}
