package xyz.hstudio.apexbattle.nms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface INMS {

    ItemStack addUniqueTag(final ItemStack itemStack, final String tag);

    boolean hasUniqueTag(final ItemStack itemStack, final String tag);

    String getUniqueTag(final ItemStack itemStack);

    ItemStack removeUniqueTag(final ItemStack itemStack);

    void sendTitle(final Player p, final String main, final String sub, final int fadeIn, final int stay, final int fadeOut);
}