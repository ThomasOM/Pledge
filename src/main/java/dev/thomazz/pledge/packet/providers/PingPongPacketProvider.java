package dev.thomazz.pledge.packet.providers;

import dev.thomazz.pledge.packet.PingPacketProvider;
import dev.thomazz.pledge.util.MinecraftReflection;
import dev.thomazz.pledge.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class PingPongPacketProvider implements PingPacketProvider {
    private final Class<?> pingClass;
    private final Class<?> pongClass;

    private final Field pongIdField;
    private final Constructor<?> pingConstructor;

    public PingPongPacketProvider() throws Exception {
        this.pingClass = MinecraftReflection.gamePacket("ClientboundPingPacket");
        this.pongClass = MinecraftReflection.gamePacket("ServerboundPongPacket");

        this.pongIdField = ReflectionUtil.getFieldByType(this.pongClass, int.class);
        this.pingConstructor = this.pingClass.getConstructor(int.class);
    }

    @Override
    public Object buildPacket(int id) throws Exception {
        return this.pingConstructor.newInstance(id);
    }

    @Override
    public int idFromPong(Object packet) throws Exception {
        return this.pongIdField.getInt(packet);
    }

    @Override
    public boolean isPong(Object packet) {
        return this.pongClass.isInstance(packet);
    }

    @Override
    public int getLowerBound() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getUpperBound() {
        return Integer.MAX_VALUE;
    }
}
