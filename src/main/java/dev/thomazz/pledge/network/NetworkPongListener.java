package dev.thomazz.pledge.network;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.event.PongReceiveEvent;
import dev.thomazz.pledge.packet.PingPacketProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class NetworkPongListener extends ChannelInboundHandlerAdapter {
    private final PledgeImpl clientPing;
    private final Player player;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PingPacketProvider packetProvider = this.clientPing.getPacketProvider();

        if (packetProvider.isPong(msg)) {
            int id = packetProvider.idFromPong(msg);
            Bukkit.getServer().getPluginManager().callEvent(new PongReceiveEvent(this.player, id));
        }

        super.channelRead(ctx, msg);
    }
}
