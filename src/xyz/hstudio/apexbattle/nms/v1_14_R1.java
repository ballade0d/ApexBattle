package xyz.hstudio.apexbattle.nms;

import io.netty.channel.ChannelPipeline;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

@SuppressWarnings("all")
public class v1_14_R1 implements INMS {

    @Override
    public ChannelPipeline getPipeline(Player p) {
        return ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
    }

    @Override
    public boolean convertIn(final Player p, final Object packet) {
        return false;
    }
}