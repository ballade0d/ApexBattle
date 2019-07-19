package xyz.hstudio.apexbattle.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.annotation.LoadFromConfig;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    @LoadFromConfig(path = "wait_time")
    public int wait_time;
    @LoadFromConfig(path = "special.ENDER_PEARL.vec_multiplier")
    public int special_ender_pearl_vec_multiplier;
    @LoadFromConfig(path = "special.ELYTRA.y_addition")
    public int special_elytra_y_addition;

    public Map<String, Map.Entry<Integer, ItemStack>> resourceMap;

    public ConfigManager() {
        FileConfiguration config = ApexBattle.getInstance().getConfig();
        ConfigLoader.load(this, config);

        for (String type : config.getConfigurationSection("resource").getKeys(false)) {
            String name = config.getString("resource." + type + ".name");
            Material material = Material.getMaterial(config.getString("resource." + type + ".material"));
            boolean ench = config.getBoolean("resource." + type + ".ench");
            List<String> lore = config.getStringList("resource." + type + ".list");
            ItemStack itemStack = new ItemStack(material);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(name);
            itemMeta.setLore(lore);
            if (ench) {
                itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            itemStack.setItemMeta(itemMeta);

            int interval = config.getInt("resource." + type + ".interval");
            resourceMap.put(type, new AbstractMap.SimpleEntry<>(interval, itemStack));
        }
    }
}