package dev.thomazz.pledge.pinger;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.pinger.data.Ping;
import dev.thomazz.pledge.pinger.data.PingOrder;

public class ClientPingerImpl extends AbstractClientPinger {

    public ClientPingerImpl(PledgeImpl clientPing, int startId, int endId) {
        super(clientPing, startId, endId);
    }

    @Override
    public void tickStart() {
        this.pingDataMap.forEach((player, data) -> this.ping(player, new Ping(PingOrder.TICK_START, data.pullId())));
    }

    @Override
    public void tickEnd() {
        this.pingDataMap.forEach((player, data) -> this.ping(player, new Ping(PingOrder.TICK_END, data.pullId())));
    }
}
