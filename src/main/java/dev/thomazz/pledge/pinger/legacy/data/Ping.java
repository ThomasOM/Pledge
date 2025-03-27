package dev.thomazz.pledge.pinger.legacy.data;

import lombok.Data;

@Data
public class Ping {
    private final PingOrder order;
    private final int id;
}
