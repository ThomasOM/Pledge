package dev.thomazz.pledge.api.event;

import dev.thomazz.pledge.api.PacketFrame;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

@Getter
public abstract class PacketFrameEvent extends Event {
    private final Player player;
    private final PacketFrame frame;

    protected PacketFrameEvent(Player player, PacketFrame frame, boolean async) {
        super(async);
        this.player = player;
        this.frame = frame;
    }
}
