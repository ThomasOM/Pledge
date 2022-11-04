package dev.thomazz.pledge.packet.providers;

import dev.thomazz.pledge.packet.PacketProvider;
import dev.thomazz.pledge.util.MinecraftUtil;
import dev.thomazz.pledge.util.ReflectionUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class PingPongPacketProvider implements PacketProvider {
	private final Class<?> pingClass;
	private final Class<?> pongClass;
	private final Class<?> keepAliveClass;
	private final Class<?> disconnectClass;

	private final Field pongIdField;
	private final Constructor<?> pingConstructor;

	public PingPongPacketProvider() throws Exception {
		this.pingClass = MinecraftUtil.gamePacket("ClientboundPingPacket");
		this.pongClass = MinecraftUtil.gamePacket("ServerboundPongPacket");
		this.keepAliveClass = MinecraftUtil.gamePacket("PacketPlayOutKeepAlive");
		this.disconnectClass = MinecraftUtil.gamePacket("PacketPlayOutKickDisconnect");

		this.pongIdField = ReflectionUtil.getFieldByType(this.pongClass, int.class);
		this.pingConstructor = this.pingClass.getConstructor(int.class);
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
	public boolean isKeepAlive(Object packet) {
		return this.keepAliveClass.isInstance(packet);
	}

	@Override
	public boolean isDisconnect(Object packet) {
		return this.disconnectClass.isInstance(packet);
	}
}
