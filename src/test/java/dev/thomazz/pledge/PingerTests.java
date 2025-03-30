package dev.thomazz.pledge;

import dev.thomazz.pledge.event.PongReceiveEvent; 
import dev.thomazz.pledge.packet.ping.PingPacketProvider;
import dev.thomazz.pledge.pinger.AbstractPinger;
import dev.thomazz.pledge.pinger.PingerOptions;
import dev.thomazz.pledge.pinger.impl.BundlePingerImpl;
import dev.thomazz.pledge.pinger.impl.LegacyPingerImpl;
import io.netty.channel.embedded.EmbeddedChannel;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@DisplayName("Pinger Tests")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PingerTests {
    @Mock private PledgeImpl api;
    @Mock private PingPacketProvider pingProvider;
    @Mock private Player player;

    private EmbeddedChannel channel;

    @BeforeEach
    public void setupMocks() {
        when(this.api.getPingProvider()).thenReturn(this.pingProvider);
        when(this.pingProvider.getUpperBound()).thenReturn(0);
        when(this.pingProvider.getLowerBound()).thenReturn(-999);
        this.channel = new EmbeddedChannel();
    }

    @Test
    public void testLegacyPinger() {
        AbstractPinger pinger = new LegacyPingerImpl(this.api, this.player, this.channel, PingerOptions.range(0, -999));

        for (int i = 0; i < 200; i++) {
            pinger.onTickStart();
            pinger.onTickEnd();
            this.channel.runPendingTasks();
        }

        // Assert 400 ping being sent
        assertEquals(-400, pinger.getId());

        for (int i = 0; i > -400; i--) {
            PongReceiveEvent event = new PongReceiveEvent(this.player, i);
            if (pinger.isInRange(i)) {
                pinger.onPongReceive(event);
            }

            if (event.getContext() == null || !event.getContext().isValid()) {
                fail("Could not find id: " + i);
            }
        }

        for (int i = 0; i < 400; i++) {
            pinger.onTickStart();
            pinger.onTickEnd();
            this.channel.runPendingTasks();
        }

        // Assert range overflow
        assertEquals(-200, pinger.getId());
    }

    @Test
    public void testBundlePinger() {
        AbstractPinger pinger = new BundlePingerImpl(this.api, this.player, this.channel, PingerOptions.range(0, -999));

        pinger.onTickEnd();
        for (int i = 0; i < 600; i++) {
            pinger.onTickStart();
            pinger.onTickEnd();
            this.channel.runPendingTasks();
        }

        // Assert 400 ping being sent
        assertEquals(-600, pinger.getId());

        for (int i = 0; i > -600; i--) {
            PongReceiveEvent event = new PongReceiveEvent(this.player, i);
            if (pinger.isInRange(i)) {
                pinger.onPongReceive(event);
            }

            if (event.getContext() == null || !event.getContext().isValid()) {
                fail("Could not find id: " + i);
            }
        }

        for (int i = 0; i < 600; i++) {
            pinger.onTickStart();
            pinger.onTickEnd();
            this.channel.runPendingTasks();
        }

        // Assert range overflow
        assertEquals(-200, pinger.getId());
    }
}
