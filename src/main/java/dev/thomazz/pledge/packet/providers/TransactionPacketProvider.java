package dev.thomazz.pledge.packet.providers;

import dev.thomazz.pledge.packet.PingPacketProvider;
import dev.thomazz.pledge.util.MinecraftReflection;
import dev.thomazz.pledge.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class TransactionPacketProvider implements PingPacketProvider {
    private final Class<?> inTransactionClass;
    private final Class<?> outTransactionClass;

    private final Field inTransactionIdField;
    private final Constructor<?> outTransactionConstructor;

    public TransactionPacketProvider() throws Exception {
        this.inTransactionClass = MinecraftReflection.gamePacket("PacketPlayInTransaction");
        this.outTransactionClass = MinecraftReflection.gamePacket("PacketPlayOutTransaction");

        this.inTransactionIdField = ReflectionUtil.getFieldByType(this.inTransactionClass, short.class);
        this.outTransactionConstructor = this.outTransactionClass.getConstructor(int.class, short.class, boolean.class);
    }

    @Override
    public Object buildPacket(int id) throws Exception {
        return this.outTransactionConstructor.newInstance(0, (short) id, false);
    }

    @Override
    public int idFromPong(Object packet) throws Exception {
        return this.inTransactionIdField.getShort(packet);
    }

    @Override
    public boolean isPong(Object packet) throws Exception {
        return this.inTransactionClass.isInstance(packet) && this.inTransactionIdField.getShort(packet) < 0;
    }

    @Override
    public int getLowerBound() {
        return Short.MIN_VALUE;
    }

    @Override
    public int getUpperBound() {
        return -1;
    }
}
