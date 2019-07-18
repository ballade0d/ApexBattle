package xyz.hstudio.apexbattle.nms;

import io.netty.channel.ChannelPipeline;
import org.bukkit.entity.Player;

public interface INMS {

    ChannelPipeline getPipeline(final Player p);

    boolean convertIn(final Player p, final Object packet);
}