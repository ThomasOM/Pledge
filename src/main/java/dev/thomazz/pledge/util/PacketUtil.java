package dev.thomazz.pledge.util;

import dev.thomazz.pledge.PledgeImpl;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings({"ConstantConditions", "SameParameterValue"})
public final class PacketUtil {
    public static final Class<?> IN_TRANSACTION_CLASS = PacketUtil.exceptionWrapCached(() -> MinecraftUtil.legacyNms("PacketPlayInTransaction"), false);
    public static final Class<?> OUT_TRANSACTION_CLASS = PacketUtil.exceptionWrapCached(() -> MinecraftUtil.legacyNms("PacketPlayOutTransaction"), false);

    public static final Class<?> PONG_CLASS = PacketUtil.exceptionWrapCached(() -> MinecraftUtil.gamePacket("ServerboundPongPacket"), false);
    public static final Class<?> PING_CLASS = PacketUtil.exceptionWrapCached(() -> MinecraftUtil.gamePacket("ClientboundPingPacket"), false);

    public static final PacketMode MODE = PacketUtil.IN_TRANSACTION_CLASS != null ? PacketMode.TRANSACTION : PacketMode.PING_PONG;

    // We use method handles because we're gonna call these very frequently
    private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static final MethodHandle OUT_TRANSACTION_CONSTRUCTOR = PacketUtil.constructorFromData(PacketUtil.OUT_TRANSACTION_CLASS, MethodType.methodType(void.class));
    public static final MethodHandle PING_CONSTRUCTOR = PacketUtil.constructorFromData(PacketUtil.PING_CLASS, MethodType.methodType(void.class, int.class));

    // Method handles for transaction packets
    public static final MethodHandle IN_WINDOW_FIELD_GET = PacketUtil.getterFromData(PacketUtil.IN_TRANSACTION_CLASS, int.class, 0);
    public static final MethodHandle IN_ACCEPT_FIELD_GET = PacketUtil.getterFromData(PacketUtil.IN_TRANSACTION_CLASS, boolean.class, 0);
    public static final MethodHandle IN_ACTION_FIELD_GET = PacketUtil.getterFromData(PacketUtil.IN_TRANSACTION_CLASS, short.class, 0);

    public static final MethodHandle OUT_WINDOW_FIELD_SET = PacketUtil.setterFromData(PacketUtil.OUT_TRANSACTION_CLASS, int.class, 0);
    public static final MethodHandle OUT_ACCEPT_FIELD_SET = PacketUtil.setterFromData(PacketUtil.OUT_TRANSACTION_CLASS, boolean.class, 0);
    public static final MethodHandle OUT_ACTION_FIELD_SET = PacketUtil.setterFromData(PacketUtil.OUT_TRANSACTION_CLASS, short.class, 0);

    public static final MethodHandle OUT_WINDOW_FIELD_GET = PacketUtil.getterFromData(PacketUtil.OUT_TRANSACTION_CLASS, int.class, 0);
    public static final MethodHandle OUT_ACCEPT_FIELD_GET = PacketUtil.getterFromData(PacketUtil.OUT_TRANSACTION_CLASS, boolean.class, 0);
    public static final MethodHandle OUT_ACTION_FIELD_GET = PacketUtil.getterFromData(PacketUtil.OUT_TRANSACTION_CLASS, short.class, 0);

    public static int TRANSACTION_WINDOW_ID = 0;
    public static boolean TRANSACTION_ACCEPT_STATE = false;

    // Method handles for ping/pong packets
    public static final MethodHandle PONG_ID_FIELD_GET = PacketUtil.getterFromData(PacketUtil.PONG_CLASS, int.class, 0);
    public static final MethodHandle PING_ID_FIELD_SET = PacketUtil.setterFromData(PacketUtil.PING_CLASS, int.class, 0);
    public static final MethodHandle PING_ID_FIELD_GET = PacketUtil.getterFromData(PacketUtil.PING_CLASS, int.class, 0);

    // Dynamically initialized
    public static MethodHandle CONNECTION_SEND_PACKET;

    private static MethodHandle getterFromData(Class<?> clazz, Class<?> fieldType, int index) {
        return PacketUtil.exceptionWrapCached(() -> {
            if (clazz != null) {
                Field field = ReflectionUtil.get(clazz, fieldType, index);
                field.setAccessible(true);
                return PacketUtil.LOOKUP.unreflectGetter(field);
            } else {
                return null;
            }
        });
    }

    private static MethodHandle setterFromData(Class<?> clazz, Class<?> fieldType, int index) {
        return PacketUtil.exceptionWrapCached(() -> {
            if (clazz != null) {
                Field field = ReflectionUtil.get(clazz, fieldType, index);
                field.setAccessible(true);
                return PacketUtil.LOOKUP.unreflectSetter(field);
            } else {
                return null;
            }
        });
    }

    private static MethodHandle constructorFromData(Class<?> clazz, MethodType type) {
        return PacketUtil.exceptionWrapCached(() -> {
            if (clazz != null) {
                return PacketUtil.LOOKUP.findConstructor(clazz, type);
            } else {
                return null;
            }
        });
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

    public static void sendPacket(Object playerConnection, Object packet) {
        try {
            if (PacketUtil.CONNECTION_SEND_PACKET == null) {
                Method method = ReflectionUtil.getMethodByNames(playerConnection.getClass(), "sendPacket", "a");
                PacketUtil.CONNECTION_SEND_PACKET = PacketUtil.LOOKUP.unreflect(method);
            }

            PacketUtil.CONNECTION_SEND_PACKET.invoke(playerConnection, packet);
        } catch (Throwable throwable) {
            PledgeImpl.LOGGER.severe("Could not send packet!");
            throwable.printStackTrace();
        }
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
