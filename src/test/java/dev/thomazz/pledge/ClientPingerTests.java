package dev.thomazz.pledge;

import dev.thomazz.pledge.packet.ping.PingPacketProvider;
import dev.thomazz.pledge.pinger.ClientPingerImpl;
import dev.thomazz.pledge.pinger.ClientPingerListener;
import dev.thomazz.pledge.pinger.ClientPingerOptions;
import dev.thomazz.pledge.pinger.data.PingData;
import io.netty.channel.embedded.EmbeddedChannel;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Client Pinger Tests")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientPingerTests {
    @Mock private PledgeImpl clientPing;
    @Mock private PingPacketProvider provider;
    @Mock private Player player;

    private EmbeddedChannel channel;

    @BeforeEach
    public void setupMocks() {
        when(this.clientPing.getPacketProvider()).thenReturn(this.provider);
        when(this.clientPing.getChannel(this.player)).thenReturn(Optional.of(this.channel = new EmbeddedChannel()));

        when(this.provider.getUpperBound()).thenReturn(0);
        when(this.provider.getLowerBound()).thenReturn(-999);
    }

    @Test
    @Order(1)
    public void testClientPinger() {
        ClientPingerImpl pinger = new ClientPingerImpl(this.clientPing, ClientPingerOptions.range(0, -999));

        pinger.registerPlayer(this.player);

        PingData pingData = pinger.getPingData(this.player).orElseThrow(IllegalStateException::new);

        for (int i = 0; i < 200; i++) {
            pinger.tickStart();
            pinger.tickEnd();
            this.channel.runPendingTasks();
        }

        // Assert 400 ping being sent
        assertEquals(-400, pingData.getId());

        for (int i = 0; i > -400; i--) {
            if (!pingData.confirm(i).isPresent()) {
                fail("Not found: " + i);
            }
        }

        for (int i = 0; i < 400; i++) {
            pinger.tickStart();
            pinger.tickEnd();
            this.channel.runPendingTasks();
        }

        // Assert range overflow
        assertEquals(-200, pingData.getId());
    }

    @Test
    @Order(2)
    public void testClientPingerListener() {
        ClientPingerImpl pinger = new ClientPingerImpl(this.clientPing, ClientPingerOptions.range(0, -999));

        pinger.registerPlayer(this.player);

        PingData pingData = pinger.getPingData(this.player).orElseThrow(IllegalStateException::new);

        ClientPingerListener listener = mock(ClientPingerListener.class);
        pinger.attach(listener);

        for (int i = 0; i < 2; i++) {
            pinger.tickStart();
            pinger.tickEnd();
            this.channel.runPendingTasks();
        }

        pinger.onReceive(this.player, pingData.confirm(0).orElseThrow(IllegalStateException::new));
        pinger.onReceive(this.player, pingData.confirm(-1).orElseThrow(IllegalStateException::new));
        pinger.onReceive(this.player, pingData.confirm(-2).orElseThrow(IllegalStateException::new));
        pinger.onReceive(this.player, pingData.confirm(-3).orElseThrow(IllegalStateException::new));

        verify(listener, times(1)).onValidation(eq(this.player), anyInt());
        verify(listener, times(2)).onPingSendStart(eq(this.player), anyInt());
        verify(listener, times(2)).onPingSendEnd(eq(this.player), anyInt());
        verify(listener, times(2)).onPongReceiveStart(eq(this.player), anyInt());
        verify(listener, times(2)).onPongReceiveEnd(eq(this.player), anyInt());
    }
}
