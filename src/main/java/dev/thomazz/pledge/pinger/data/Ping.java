package dev.thomazz.pledge.pinger.data;

import lombok.Data;

@Data
public class Ping {
    private final PingOrder order;
    private final int id;
}
