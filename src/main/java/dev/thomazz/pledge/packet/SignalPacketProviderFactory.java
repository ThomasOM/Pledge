package dev.thomazz.pledge.packet;

import dev.thomazz.pledge.packet.providers.PingPongPacketProvider;
import dev.thomazz.pledge.packet.providers.TransactionPacketProvider;

public final class SignalPacketProviderFactory {
	public static SignalPacketProvider build() {
		try {
			return new TransactionPacketProvider();
		} catch (Exception ignored) {
		}

		try {
			return new PingPongPacketProvider();
		} catch (Exception ignored) {
		}

		throw new RuntimeException("Could not set up signal packet provider!");
	}
}
