package dev.thomazz.pledge.pinger;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.annotation.Nullable;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PingerEventContext {
	@Nullable private final Order order;
	private final boolean valid;

	public enum Order {
		TICK_START,
		TICK_END
	}
}
