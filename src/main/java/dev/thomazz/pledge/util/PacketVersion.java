package dev.thomazz.pledge.util;

import org.bukkit.Bukkit;

public enum PacketVersion {
	LEGACY,
	MODERN;

	public static PacketVersion getCurrentVersion() {
		int minorVersion = Integer.parseInt(Bukkit.getVersion().split(".")[1]);
		return minorVersion >= 17 ? LEGACY : MODERN;
	}
}
