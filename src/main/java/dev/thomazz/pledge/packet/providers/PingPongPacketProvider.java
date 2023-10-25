package dev.thomazz.pledge.packet.providers;

import dev.thomazz.pledge.packet.PacketProvider;
import dev.thomazz.pledge.util.MinecraftReflection;
import dev.thomazz.pledge.util.ReflectionUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class PingPongPacketProvider implements PacketProvider {
    private final Class<?> pingClass;
    private final Class<?> pongClass;
    private final Class<?> loginClass;
    private final Class<?> keepAliveClass;
    private final Class<?> disconnectClass;

    private final Field pongIdField;
    private final Constructor<?> pingConstructor;

    public PingPongPacketProvider() throws Exception {
        this.pingClass = MinecraftReflection.gamePacket("ClientboundPingPacket");
        this.pongClass = MinecraftReflection.gamePacket("ServerboundPongPacket");
        this.loginClass = MinecraftReflection.gamePacket("PacketPlayOutLogin");
        this.keepAliveClass = getKeepAliveClass();
        this.disconnectClass = getDisconnectClass();

        this.pongIdField = ReflectionUtil.getFieldByType(this.pongClass, int.class);
        this.pingConstructor = this.pingClass.getConstructor(int.class);
    }

    private Class<?> getKeepAliveClass() throws Exception {
        try {
            return MinecraftReflection.gamePacket("PacketPlayOutKeepAlive");
        } catch (Exception ignored) {
            // 1.20.2+
            return MinecraftReflection.gamePacket("ClientboundKeepAlivePacket");
        }
    }

    private Class<?> getDisconnectClass() throws Exception {
        try {
            return MinecraftReflection.gamePacket("PacketPlayOutKickDisconnect");
        } catch (Exception ignored) {
            // 1.20.2+
            return MinecraftReflection.gamePacket("ClientboundDisconnectPacket");
        }
    }

    @Override
    public Object buildPacket(int id) throws Exception {
        return this.pingConstructor.newInstance(id);
    }

    @Override
    public Integer idFromPacket(Object packet) throws Exception {
        if (this.pongClass.isInstance(packet)) {
            return this.pongIdField.getInt(packet);
        }

        return null;
    }

    @Override
    public boolean isLogin(Object packet) {
        return this.loginClass.isInstance(packet);
    }

    @Override
    public boolean isKeepAlive(Object packet) {
        return this.keepAliveClass.isInstance(packet);
    }

    @Override
    public boolean isDisconnect(Object packet) {
        return this.disconnectClass.isInstance(packet);
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
