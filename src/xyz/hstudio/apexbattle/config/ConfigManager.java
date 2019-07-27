package xyz.hstudio.apexbattle.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.annotation.LoadFromConfig;
import xyz.hstudio.apexbattle.util.NmsUtil;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    @LoadFromConfig(path = "wait_time")
    public int wait_time;

    @LoadFromConfig(path = "protected.durability")
    public int protected_durability;

    @LoadFromConfig(path = "sign.first")
    public String sign_first;
    @LoadFromConfig(path = "sign.waiting")
    public List<String> sign_waiting;
    @LoadFromConfig(path = "sign.gaming")
    public List<String> sign_gaming;
    @LoadFromConfig(path = "sign.stop")
    public List<String> sign_stop;


    @LoadFromConfig(path = "special.ENDER_PEARL.vec_multiplier")
    public int special_ender_pearl_vec_multiplier;

    @LoadFromConfig(path = "scoreboard.title")
    public String scoreboard_title;
    @LoadFromConfig(path = "scoreboard.team_count_format")
    public String scoreboard_team_count_format;
    @LoadFromConfig(path = "scoreboard.lose_symbol")
    public String scoreboard_lose_symbol;
    @LoadFromConfig(path = "scoreboard.lines")
    public List<String> scoreboard_lines;

    public Map<String, Map.Entry<Integer, ItemStack>> resourceMap = new HashMap<>();
    public ItemStack protect;
    public ItemStack shop;

    public ConfigManager() {
        FileConfiguration config = ApexBattle.getInstance().getConfig();
        ConfigLoader.load(this, config);

        // 获取并保存资源物品
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

        // 获取被保护物品
        {
            String name = config.getString("protected.name");
            Material material = Material.getMaterial(config.getString("protected.material"));
            boolean ench = config.getBoolean("protected.ench");
            List<String> lore = config.getStringList("protected.lore");

            ItemStack itemStack = new ItemStack(material);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(name);
            itemMeta.setLore(lore);
            if (ench) {
                itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            itemStack.setItemMeta(itemMeta);
            // 添加NBT数据
            this.protect = NmsUtil.getInstance().addUniqueTag(itemStack, "Protected");
        }

        // 获取商店物品
        {
            String name = config.getString("shop.name");
            Material material = Material.getMaterial(config.getString("shop.material"));
            List<String> lore = config.getStringList("shop.lore");

            ItemStack itemStack = new ItemStack(material);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(name);
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            // 添加NBT数据
            this.shop = NmsUtil.getInstance().addUniqueTag(itemStack, "Shop");
        }
    }
}