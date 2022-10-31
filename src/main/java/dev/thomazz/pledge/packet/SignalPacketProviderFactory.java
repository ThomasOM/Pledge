package dev.thomazz.pledge.packet;

import dev.thomazz.pledge.util.PacketVersion;
import dev.thomazz.pledge.packet.providers.PingPongPacketProvider;
import dev.thomazz.pledge.packet.providers.TransactionPacketProvider;

public final class SignalPacketProviderFactory {
	public static SignalPacketProvider build() {
		try {
			switch (PacketVersion.getCurrentVersion()) {
				default:
				case LEGACY:
					return new TransactionPacketProvider();
				case MODERN:
					return new PingPongPacketProvider();
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not created packet provider!", e);
		}
	}
}
