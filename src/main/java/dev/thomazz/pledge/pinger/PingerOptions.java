package dev.thomazz.pledge.pinger;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Options for creating a {@link Pinger}
 * <p>
 * {@link PingerOptions#startId} - Start ID for ping range
 * {@link PingerOptions#endId} - End ID for ping range
 * {@link PingerOptions#useBundles} - Disable to use legacy pinger
 * {@link PingerOptions#consolidatePackets} - For legacy pinger: If asynchronous packets should be queued
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PingerOptions {
	@Builder.Default
	private final int startId = -1000;
	@Builder.Default
	private final int endId = -2000;
	@Builder.Default
	private boolean useBundles = true;
	@Builder.Default
	private boolean consolidatePackets = true;

	public static PingerOptions range(int startId, int endId) {
		return PingerOptions.builder()
			.startId(startId)
			.endId(endId)
			.build();
	}
}
