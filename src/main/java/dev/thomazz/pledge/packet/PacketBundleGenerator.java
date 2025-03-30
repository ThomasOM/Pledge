package dev.thomazz.pledge.packet;

import dev.thomazz.pledge.util.MinecraftReflection;

import java.lang.reflect.Constructor;

public class PacketBundleGenerator {
	private Constructor<?> bundlePacketConstructor;

	public PacketBundleGenerator() {
		try {
			Class<?> bundlePacketClass = MinecraftReflection.gamePacket("BundleDelimiterPacket");
			this.bundlePacketConstructor = bundlePacketClass.getConstructor();
		} catch (Exception ignored) {
		}
	}

	public Object buildBundlePacket() {
		try {
			return this.bundlePacketConstructor.newInstance();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to build bundle packet!", ex);
		}
	}

	public boolean isSupported() {
		return this.bundlePacketConstructor != null;
	}
}
