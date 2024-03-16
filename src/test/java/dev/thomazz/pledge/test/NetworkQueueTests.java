package dev.thomazz.pledge.test;

import dev.thomazz.pledge.network.queue.ChannelMessageQueueHandler;
import dev.thomazz.pledge.network.queue.QueueMode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Network Queue Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NetworkQueueTests {
    private EmbeddedChannel channel;
    private ChannelMessageQueueHandler queueHandler;
    private TestChannelOutboundHandler testHandler;

    @BeforeEach
    public void setup() {
        this.testHandler = new TestChannelOutboundHandler();
        this.queueHandler = new ChannelMessageQueueHandler();
        this.channel = new EmbeddedChannel(this.testHandler, this.queueHandler);

        this.queueHandler.setMode(QueueMode.ADD_LAST);
        this.channel.writeAndFlush("test2");
        this.channel.writeAndFlush("test3");
        this.channel.writeAndFlush("test4");
        this.queueHandler.setMode(QueueMode.ADD_FIRST);
        this.channel.writeAndFlush("test1");
        this.queueHandler.setMode(QueueMode.ADD_LAST);
        this.channel.writeAndFlush("test5");
    }

    @Test
    @Order(1)
    public void testDrain() {
        this.queueHandler.drain(this.channel.pipeline().lastContext());

        int i = 1;
        for (Object message : this.testHandler.messages) {
            assertEquals("test" + i++, message);
        }
    }

    @Test
    @Order(2)
    public void testClose() {
        this.channel.close();

        int i = 1;
        for (Object message : this.testHandler.messages) {
            assertEquals("test" + i++, message);
        }
    }

    private static class TestChannelOutboundHandler extends ChannelOutboundHandlerAdapter {
        private final Queue<Object> messages = new ConcurrentLinkedQueue<>();

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            this.messages.add(msg);
        }
    }
}
