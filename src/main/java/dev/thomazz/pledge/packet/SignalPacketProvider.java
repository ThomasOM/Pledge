package dev.thomazz.pledge.packet;

public interface SignalPacketProvider {
	Object buildPacket(int id) throws Exception;

	Integer idFromPacket(Object packet) throws Exception;
}
