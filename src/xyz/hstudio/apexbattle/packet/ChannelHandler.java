package xyz.hstudio.apexbattle.packet;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_12_R1.DataWatcher;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.List;

public class ChannelHandler extends ChannelDuplexHandler {

    private Player p;

    private ChannelHandler(Player p) {
        this.p = p;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
        super.channelRead(context, packet);
    }

    @Override
    public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {
        if (packet instanceof PacketPlayOutEntityMetadata) {
            PacketPlayOutEntityMetadata nmsPacket = (PacketPlayOutEntityMetadata) packet;
            PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(0));
            try {
                nmsPacket.b(serializer);
                int id = serializer.g();
                List<DataWatcher.Item<?>> list = DataWatcher.b(serializer);
                if (list != null) {
                    List<DataWatcher.Item<?>> watchableObjects = new ArrayList<>(list);
                    Entity entity = ((CraftWorld) p.getWorld()).getHandle().getEntity(id);
                    CraftEntity craftEntity = entity == null ? null : entity.getBukkitEntity();
                    if (entity != null && !(craftEntity instanceof Wither) &&
                            !(craftEntity instanceof EnderDragon)
                            && (craftEntity instanceof HumanEntity ||
                            craftEntity instanceof Monster ||
                            craftEntity instanceof Animals ||
                            craftEntity instanceof Golem)) {
                        if (id != p.getEntityId()) {
                            DataWatcher.Item watchableObject;
                            for (int i = 0, len = watchableObjects.size(); i < len; i++) {
                                watchableObject = watchableObjects.get(i);
                                if (watchableObject.b() instanceof Float && watchableObject.a().a() == 7 && (float) watchableObject.b() > 0.0F) {
                                    watchableObject.a(craftEntity instanceof Villager ? 20 : Float.NaN);
                                    watchableObjects.set(i, watchableObject);

                                    PacketDataSerializer writer = new PacketDataSerializer(Unpooled.buffer(256));
                                    writer.d(id);
                                    DataWatcher.a(watchableObjects, writer);
                                    PacketPlayOutEntityMetadata newPacket = new PacketPlayOutEntityMetadata();
                                    newPacket.a(writer);
                                    packet = newPacket;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.write(context, packet, promise);
    }

    public static void register(final Player p) {
        ChannelPipeline channelPipeline = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
        ChannelDuplexHandler channelDuplexHandler = new ChannelHandler(p);
        String handlerName = "apex_packet_handler";
        if (channelPipeline.get(handlerName) != null) {
            channelPipeline.remove(handlerName);
        }
        channelPipeline.addBefore("packet_handler", handlerName, channelDuplexHandler);
    }
}