package xyz.hstudio.apexbattle.packet;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import xyz.hstudio.apexbattle.util.NmsUtil;

@RequiredArgsConstructor
public class ChannelHandler extends ChannelDuplexHandler {

    private final Player p;

    @Override
    public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
        try {
            if (NmsUtil.getInstance().convertIn(p, packet)) {
                return;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        super.channelRead(context, packet);
    }

    @Override
    public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {
        super.write(context, packet, promise);
    }

    public static void register(final Player p) {
        ChannelPipeline channelPipeline = NmsUtil.getInstance().getPipeline(p);
        ChannelDuplexHandler channelDuplexHandler = new ChannelHandler(p);
        String handlerName = "apex_packet_handler";
        if (channelPipeline.get(handlerName) != null) {
            channelPipeline.remove(handlerName);
        }
        channelPipeline.addBefore("packet_handler", handlerName, channelDuplexHandler);
    }
}