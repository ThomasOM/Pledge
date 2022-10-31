package dev.thomazz.pledge.packet.providers;

import dev.thomazz.pledge.packet.SignalPacketProvider;
import dev.thomazz.pledge.util.MinecraftUtil;
import dev.thomazz.pledge.util.ReflectionUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class PingPongPacketProvider implements SignalPacketProvider {
	private final Class<?> pongClass;
	private final Class<?> pingClass;

	private final Field pongIdField;
	private final Constructor<?> pingConstructor;

	public PingPongPacketProvider() throws Exception {
		this.pongClass = MinecraftUtil.gamePacket("ServerboundPongPacket");
		this.pingClass = MinecraftUtil.gamePacket("ClientboundPingPacket");

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
}
