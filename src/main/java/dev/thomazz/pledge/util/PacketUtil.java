package dev.thomazz.pledge.util;

import dev.thomazz.pledge.PledgeImpl;
import java.lang.reflect.Field;

public final class PacketUtil {
    // Cache reflection stuff to make this faster
    public static Class<?> IN_TRANSACTION_CLASS;
    public static Class<?> OUT_TRANSACTION_CLASS;

    public static Field IN_WINDOW_FIELD;
    public static Field IN_ACCEPT_FIELD;
    public static Field IN_ACTION_FIELD;

    public static Field OUT_WINDOW_FIELD;
    public static Field OUT_ACCEPT_FIELD;
    public static Field OUT_ACTION_FIELD;

    // Defaults that are used
    private static int TRANSACTION_WINDOW_ID = 0;
    private static boolean TRANSACTION_ACCEPT_STATE = false;

    static {
        try {
            PacketUtil.IN_TRANSACTION_CLASS = MinecraftUtil.nms("PacketPlayInTransaction");
            PacketUtil.OUT_TRANSACTION_CLASS = MinecraftUtil.nms("PacketPlayOutTransaction");

            PacketUtil.IN_WINDOW_FIELD = ReflectionUtil.get(PacketUtil.IN_TRANSACTION_CLASS, int.class, 0);
            PacketUtil.IN_ACCEPT_FIELD = ReflectionUtil.get(PacketUtil.IN_TRANSACTION_CLASS, boolean.class, 0);
            PacketUtil.IN_ACTION_FIELD = ReflectionUtil.get(PacketUtil.IN_TRANSACTION_CLASS, short.class, 0);

            PacketUtil.OUT_WINDOW_FIELD = ReflectionUtil.get(PacketUtil.OUT_TRANSACTION_CLASS, int.class, 0);
            PacketUtil.OUT_ACCEPT_FIELD = ReflectionUtil.get(PacketUtil.OUT_TRANSACTION_CLASS, boolean.class, 0);
            PacketUtil.OUT_ACTION_FIELD = ReflectionUtil.get(PacketUtil.OUT_TRANSACTION_CLASS, short.class, 0);
        } catch (Exception e) {
            PledgeImpl.LOGGER.severe("Could not initialize transaction values!");
            e.printStackTrace();
        }
    }

    public static Object buildTransactionPacket(short actionNumber) {
        try {
            Object packet = PacketUtil.OUT_TRANSACTION_CLASS.newInstance();

            PacketUtil.OUT_WINDOW_FIELD.set(packet, PacketUtil.TRANSACTION_WINDOW_ID);
            PacketUtil.OUT_ACCEPT_FIELD.set(packet, PacketUtil.TRANSACTION_ACCEPT_STATE);
            PacketUtil.OUT_ACTION_FIELD.set(packet, actionNumber);

            return packet;
        } catch (IllegalAccessException | InstantiationException e) {
            PledgeImpl.LOGGER.severe("Could not build transaction packet!");
            e.printStackTrace();
        }

        return null;
    }

}
