package xyz.hstudio.apexbattle.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    public static boolean isSimilar(final ItemStack stack1, final ItemStack stack2) {
        if (stack1 == null || stack2 == null) {
            return false;
        } else if (stack1 == stack2) {
            return true;
        } else {
            return stack1.getTypeId() == stack2.getTypeId() && stack1.hasItemMeta() && stack2.hasItemMeta() && Bukkit.getItemFactory().equals(stack1.getItemMeta(), stack2.getItemMeta());
        }
    }
}