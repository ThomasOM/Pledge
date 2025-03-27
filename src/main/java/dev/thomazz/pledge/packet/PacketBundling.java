package dev.thomazz.pledge.packet;

import dev.thomazz.pledge.util.MinecraftReflection;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;

@UtilityClass
public class PacketBundling {
	private Constructor<?> bundlePacketConstructor;

	public Object buildBundlePacket() throws Exception {
		return PacketBundling.getBundlePacketConstructor().newInstance();
	}

	private Constructor<?> getBundlePacketConstructor() throws Exception {
		if (PacketBundling.bundlePacketConstructor == null) {
			Class<?> bundlePacketClass = MinecraftReflection.gamePacket("BundleDelimiterPacket");
			PacketBundling.bundlePacketConstructor = bundlePacketClass.getConstructor();
		}

		return PacketBundling.bundlePacketConstructor;
	}
}
