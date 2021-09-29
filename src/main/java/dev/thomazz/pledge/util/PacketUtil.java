package dev.thomazz.pledge.util;

import dev.thomazz.pledge.PledgeImpl;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

@SuppressWarnings({"ConstantConditions", "SameParameterValue"})
public final class PacketUtil {
    public static final Class<?> IN_TRANSACTION_CLASS = PacketUtil.exceptionWrapCached(() -> MinecraftUtil.legacyNms("PacketPlayInTransaction"), false);
    public static final Class<?> OUT_TRANSACTION_CLASS = PacketUtil.exceptionWrapCached(() -> MinecraftUtil.legacyNms("PacketPlayOutTransaction"), false);

    public static final Class<?> PONG_CLASS = PacketUtil.exceptionWrapCached(() -> MinecraftUtil.gamePacket("ServerboundPongPacket"), false);
    public static final Class<?> PING_CLASS = PacketUtil.exceptionWrapCached(() -> MinecraftUtil.gamePacket("ClientboundPingPacket"), false);

    public static final PacketMode MODE = PacketUtil.IN_TRANSACTION_CLASS != null ? PacketMode.TRANSACTION : PacketMode.PING_PONG;

    // We use method handles because we're gonna call these very frequently
    private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // Method handles for transaction packets
    public static final MethodHandle IN_WINDOW_FIELD_GET = PacketUtil.exceptionWrapCached(() -> PacketUtil.getterFromData(PacketUtil.IN_TRANSACTION_CLASS, int.class, 0));
    public static final MethodHandle IN_ACCEPT_FIELD_GET = PacketUtil.exceptionWrapCached(() -> PacketUtil.getterFromData(PacketUtil.IN_TRANSACTION_CLASS, boolean.class, 0));
    public static final MethodHandle IN_ACTION_FIELD_GET = PacketUtil.exceptionWrapCached(() -> PacketUtil.getterFromData(PacketUtil.IN_TRANSACTION_CLASS, short.class, 0));

    public static final MethodHandle OUT_WINDOW_FIELD_SET = PacketUtil.exceptionWrapCached(() -> PacketUtil.setterFromData(PacketUtil.OUT_TRANSACTION_CLASS, int.class, 0));
    public static final MethodHandle OUT_ACCEPT_FIELD_SET = PacketUtil.exceptionWrapCached(() -> PacketUtil.setterFromData(PacketUtil.OUT_TRANSACTION_CLASS, boolean.class, 0));
    public static final MethodHandle OUT_ACTION_FIELD_SET = PacketUtil.exceptionWrapCached(() -> PacketUtil.setterFromData(PacketUtil.OUT_TRANSACTION_CLASS, short.class, 0));

    private static int TRANSACTION_WINDOW_ID = 0;
    private static boolean TRANSACTION_ACCEPT_STATE = false;

    // Method handles for ping/pong packets
    public static final MethodHandle PONG_ID_FIELD_GET = PacketUtil.exceptionWrapCached(() -> PacketUtil.getterFromData(PacketUtil.PONG_CLASS, int.class, 0));
    public static final MethodHandle PING_ID_FIELD_SET = PacketUtil.exceptionWrapCached(() -> PacketUtil.setterFromData(PacketUtil.PING_CLASS, int.class, 0));

    // We don't want to save this, just let it GC
    static {
        PacketUtil.LOOKUP = null;
    }

    private static MethodHandle getterFromData(Class<?> clazz, Class<?> fieldType, int index) throws Exception {
        if (clazz != null) {
            Field field = ReflectionUtil.get(clazz, fieldType, index);
            field.setAccessible(true);
            return PacketUtil.LOOKUP.unreflectGetter(field);
        } else {
            return null;
        }
    }

    private static MethodHandle setterFromData(Class<?> clazz, Class<?> fieldType, int index) throws Exception {
        if (clazz != null) {
            Field field = ReflectionUtil.get(clazz, fieldType, index);
            field.setAccessible(true);
            return PacketUtil.LOOKUP.unreflectSetter(field);
        } else {
            return null;
        }
    }

    private static <T> T exceptionWrapCached(ThrowingSupplier<T> supplier) {
        return PacketUtil.exceptionWrapCached(supplier, true);
    }

    private static <T> T exceptionWrapCached(ThrowingSupplier<T> supplier, boolean warn) {
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            if (warn) {
                PledgeImpl.LOGGER.severe("Exception setting up packet cache!");
                throwable.printStackTrace();
            }
        }

        return null;
    }

    public static Object buildTransactionPacket(int actionNumber) {
        try {
            Object packet;
            switch (PacketUtil.MODE) {
                default:
                case TRANSACTION:
                    packet = PacketUtil.OUT_TRANSACTION_CLASS.newInstance();

                    PacketUtil.OUT_WINDOW_FIELD_SET.invoke(packet, PacketUtil.TRANSACTION_WINDOW_ID);
                    PacketUtil.OUT_ACCEPT_FIELD_SET.invoke(packet, PacketUtil.TRANSACTION_ACCEPT_STATE);
                    PacketUtil.OUT_ACTION_FIELD_SET.invoke(packet, (short) actionNumber);
                    break;
                case PING_PONG:
                    packet = ReflectionUtil.instantiateUnsafe(PacketUtil.PING_CLASS);

                    PacketUtil.PING_ID_FIELD_SET.invoke(packet, actionNumber);
                    break;
            }

            return packet;
        } catch (Throwable throwable) {
            PledgeImpl.LOGGER.severe("Could not build transaction packet!");
            throwable.printStackTrace();
        }

        return null;
    }

    public static void wakeUp() {
        // Dummy method to initialize all static variables
    }

    private interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }

    // We normally use transactions, in 1.17+ we use ping/pong packets
    public enum PacketMode {
        TRANSACTION,
        PING_PONG
    }
}
