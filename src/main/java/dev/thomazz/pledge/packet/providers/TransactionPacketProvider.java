package dev.thomazz.pledge.packet.providers;

import dev.thomazz.pledge.packet.SignalPacketProvider;
import dev.thomazz.pledge.util.MinecraftUtil;
import dev.thomazz.pledge.util.ReflectionUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class TransactionPacketProvider implements SignalPacketProvider {
	private final Class<?> inTransactionClass;
	private final Class<?> outTransactionClass;

	private final Field inTransactionIdField;
	private final Constructor<?> outTransactionConstructor;

	public TransactionPacketProvider() throws Exception {
		this.inTransactionClass = MinecraftUtil.legacyNms("PacketPlayInTransaction");
		this.outTransactionClass = MinecraftUtil.legacyNms("PacketPlayOutTransaction");

		this.inTransactionIdField = ReflectionUtil.getFieldByType(this.inTransactionClass, short.class);
		this.outTransactionConstructor = this.outTransactionClass.getConstructor(int.class, short.class, boolean.class);
	}

	@Override
	public Object buildPacket(int id) throws Exception {
		return this.outTransactionConstructor.newInstance(0, (short) id, true);
	}

	@Override
	public Integer idFromPacket(Object packet) throws Exception {
		if (this.inTransactionClass.isInstance(packet)) {
			return (int) this.inTransactionIdField.getShort(packet);
		}

		return null;
	}
}
