package dev.thomazz.pledge.packet;

public interface PingPacketProvider {
    Object buildPacket(int id) throws Exception;

    int idFromPong(Object packet) throws Exception;

    boolean isPong(Object packet) throws Exception;

    int getLowerBound();

    int getUpperBound();
}
