package dev.thomazz.pledge.util;

import org.bukkit.Bukkit;

public enum PacketVersion {
	LEGACY,
	MODERN;

	public static PacketVersion getCurrentVersion() {
		String versionString = Bukkit.getVersion();
		String[] split = versionString.split("[.]");
		int minorVersion = Integer.parseInt(split[1]);
		return minorVersion >= 17 ? MODERN : LEGACY;
	}
}
