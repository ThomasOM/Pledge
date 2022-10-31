package dev.thomazz.pledge.api;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PledgeSettings {
	private final PacketTickExaminer packetTickExaminer = queue -> false;
	private final int rangeStart = -2000;
	private final int rangeEnd = -3000;
}
