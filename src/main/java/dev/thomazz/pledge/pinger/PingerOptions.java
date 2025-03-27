package dev.thomazz.pledge.pinger;

import lombok.Getter;

/**
 * Options for creating a {@link Pinger}
 * <p>
 * {@link Pinger#startId} - Start ID for ping range
 * {@link Pinger#endId} - End ID for ping range
 */
@Getter
public class PingerOptions {
	protected int startId = -1000;
	protected int endId = -2000;

	public static PingerOptions range(int startId, int endId) {
		PingerOptions options = new PingerOptions();
		options.startId = startId;
		options.endId = endId;
		return options;
	}
}
