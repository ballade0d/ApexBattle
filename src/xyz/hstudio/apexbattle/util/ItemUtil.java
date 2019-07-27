package xyz.hstudio.apexbattle.util;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ItemUtil {

    public static ItemStack[] getNormalItems(final Color color) {
        LeatherArmorMeta meta;
        ItemStack head = new ItemStack(Material.LEATHER_HELMET);
        meta = (LeatherArmorMeta) head.getItemMeta();
        meta.setColor(color);
        head.setItemMeta(meta);
        head = NmsUtil.getInstance().addUniqueTag(head, "Armor");
        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        meta = (LeatherArmorMeta) chest.getItemMeta();
        meta.setColor(color);
        chest.setItemMeta(meta);
        chest = NmsUtil.getInstance().addUniqueTag(chest, "Armor");
        ItemStack leg = new ItemStack(Material.LEATHER_LEGGINGS);
        meta = (LeatherArmorMeta) leg.getItemMeta();
        meta.setColor(color);
        leg.setItemMeta(meta);
        leg = NmsUtil.getInstance().addUniqueTag(leg, "Armor");
        ItemStack boot = new ItemStack(Material.LEATHER_BOOTS);
        meta = (LeatherArmorMeta) boot.getItemMeta();
        meta.setColor(color);
        boot.setItemMeta(meta);
        boot = NmsUtil.getInstance().addUniqueTag(boot, "Armor");

        return new ItemStack[]{head, chest, leg, boot};
    }

    public static ItemStack[] getNormalTools() {
        ItemStack pickaxe = NmsUtil.getInstance().addUniqueTag(new ItemStack(Material.STONE_PICKAXE), "Armor");
        ItemStack axe = NmsUtil.getInstance().addUniqueTag(new ItemStack(Material.STONE_AXE), "Armor");
        ItemStack sword = NmsUtil.getInstance().addUniqueTag(new ItemStack(Material.STONE_SWORD), "Armor");
        ItemStack spade = NmsUtil.getInstance().addUniqueTag(new ItemStack(Material.STONE_SPADE), "Armor");
        return new ItemStack[]{pickaxe, axe, sword, spade};
    }
}