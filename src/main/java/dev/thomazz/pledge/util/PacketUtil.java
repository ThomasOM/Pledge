package dev.thomazz.pledge.util;

import dev.thomazz.pledge.PledgeImpl;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

@SuppressWarnings({"ConstantConditions", "SameParameterValue"})
public final class PacketUtil {
    public static final Class<?> IN_TRANSACTION_CLASS = PacketUtil.exceptionWrapCached(() -> MinecraftUtil.nms("PacketPlayInTransaction"));
    public static final Class<?> OUT_TRANSACTION_CLASS = PacketUtil.exceptionWrapCached(() -> MinecraftUtil.nms("PacketPlayOutTransaction"));

    // Use method handles for transaction packets, because we send these very frequently
    private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    public static final MethodHandle IN_WINDOW_FIELD_GET = PacketUtil.exceptionWrapCached(() -> PacketUtil.getterFromData(PacketUtil.IN_TRANSACTION_CLASS, int.class, 0));
    public static final MethodHandle IN_ACCEPT_FIELD_GET = PacketUtil.exceptionWrapCached(() -> PacketUtil.getterFromData(PacketUtil.IN_TRANSACTION_CLASS, boolean.class, 0));
    public static final MethodHandle IN_ACTION_FIELD_GET = PacketUtil.exceptionWrapCached(() -> PacketUtil.getterFromData(PacketUtil.IN_TRANSACTION_CLASS, short.class, 0));

    public static final MethodHandle OUT_WINDOW_FIELD_SET = PacketUtil.exceptionWrapCached(() -> PacketUtil.setterFromData(PacketUtil.OUT_TRANSACTION_CLASS, int.class, 0));
    public static final MethodHandle OUT_ACCEPT_FIELD_SET = PacketUtil.exceptionWrapCached(() -> PacketUtil.setterFromData(PacketUtil.OUT_TRANSACTION_CLASS, boolean.class, 0));
    public static final MethodHandle OUT_ACTION_FIELD_SET = PacketUtil.exceptionWrapCached(() -> PacketUtil.setterFromData(PacketUtil.OUT_TRANSACTION_CLASS, short.class, 0));

    private static int TRANSACTION_WINDOW_ID = 0;
    private static boolean TRANSACTION_ACCEPT_STATE = false;

    // We don't want to save this, just let it GC
    static {
        PacketUtil.LOOKUP = null;
    }

    private static MethodHandle getterFromData(Class<?> clazz, Class<?> fieldType, int index) throws Exception {
        Field inWindowField = ReflectionUtil.get(clazz, fieldType, index);
        inWindowField.setAccessible(true);
        return PacketUtil.LOOKUP.unreflectGetter(inWindowField);
    }

    private static MethodHandle setterFromData(Class<?> clazz, Class<?> fieldType, int index) throws Exception {
        Field inWindowField = ReflectionUtil.get(clazz, fieldType, index);
        inWindowField.setAccessible(true);
        return PacketUtil.LOOKUP.unreflectSetter(inWindowField);
    }

    private static <T> T exceptionWrapCached(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            PledgeImpl.LOGGER.severe("Could not perform caching action!");
            throwable.printStackTrace();
        }

        return null;
    }

    public static Object buildTransactionPacket(short actionNumber) {
        try {
            Object packet = PacketUtil.OUT_TRANSACTION_CLASS.newInstance();

            PacketUtil.OUT_WINDOW_FIELD_SET.invoke(packet, PacketUtil.TRANSACTION_WINDOW_ID);
            PacketUtil.OUT_ACCEPT_FIELD_SET.invoke(packet, PacketUtil.TRANSACTION_ACCEPT_STATE);
            PacketUtil.OUT_ACTION_FIELD_SET.invoke(packet, actionNumber);

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
}
