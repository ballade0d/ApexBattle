package xyz.hstudio.apexbattle.game.internal;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ProtectedItem {

    private final String name;
    private final Material material;
    private final boolean ench;
    private final List<String> lore;
    private final int durability;
    @Getter
    private final int durability_decrease;

    ProtectedItem(final String name, final Material material, final boolean ench, final List<String> lore, final int durability, final int durability_decrease) {
        this.name = name;
        this.material = material;
        this.ench = ench;
        this.lore = lore;
        this.durability = durability;
        this.durability_decrease = durability_decrease;
    }

    public ItemStack createItem() {
        ItemStack itemStack = new ItemStack(this.material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(this.name);

        if (this.ench) {
            itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if (!this.lore.isEmpty()) {
            itemMeta.setLore(this.lore);
        }

        itemStack.setDurability((short) this.durability);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}