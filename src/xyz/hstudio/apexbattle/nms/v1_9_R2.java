package xyz.hstudio.apexbattle.nms;

import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_9_R2.util.CraftChatMessage;
import org.bukkit.entity.Player;

@SuppressWarnings("all")
public class v1_9_R2 implements INMS {

    @Override
    public org.bukkit.inventory.ItemStack addUniqueTag(final org.bukkit.inventory.ItemStack itemStack, final String tag) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
        compound.set("ApexBattle", new NBTTagString(tag));
        nmsItem.setTag(compound);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public boolean hasUniqueTag(org.bukkit.inventory.ItemStack itemStack, final String tag) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (!nmsItem.hasTag()) {
            return false;
        }
        NBTTagCompound compound = nmsItem.getTag();
        NBTBase base = compound.get("ApexBattle");
        if (base != null && base.toString().equals("\"" + tag + "\"")) {
            return true;
        }
        return false;
    }

    @Override
    public String getUniqueTag(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (!nmsItem.hasTag()) {
            return null;
        }
        NBTTagCompound compound = nmsItem.getTag();
        NBTBase base = compound.get("ApexBattle");
        if (base != null) {
            String string = base.toString();
            return string.substring(1, string.length() - 1);
        }
        return null;
    }

    @Override
    public org.bukkit.inventory.ItemStack removeUniqueTag(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (!nmsItem.hasTag()) {
            return itemStack;
        }
        NBTTagCompound compound = nmsItem.getTag();
        NBTBase base = compound.get("ApexBattle");
        if (base != null) {
            compound.remove("ApexBattle");
        }
        nmsItem.setTag(compound);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public void sendTitle(Player p, String main, String sub, int fadeIn, int stay, int fadeOut) {
        PacketPlayOutTitle times;
        PlayerConnection playerConnection = ((CraftPlayer) p).getHandle().playerConnection;
        if (main != null) {
            times = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, CraftChatMessage.fromString(main)[0]);
            playerConnection.sendPacket(times);
        }
        if (sub != null) {
            times = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, CraftChatMessage.fromString(sub)[0]);
            playerConnection.sendPacket(times);
        }
        times = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
        playerConnection.sendPacket(times);
    }
}