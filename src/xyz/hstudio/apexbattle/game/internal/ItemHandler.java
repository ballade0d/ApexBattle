package xyz.hstudio.apexbattle.game.internal;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ItemHandler {

    public static List<ItemHandler> resource;
    public static ProtectedItem protectedItem;
    private static final Predicate<ConfigurationSection> checkConfig;
    private static final Predicate<ConfigurationSection> checkProtected;

    static {
        resource = new ArrayList<>();
        checkConfig = section ->
                section.contains("name") &&
                        section.contains("material") &&
                        section.contains("ench") &&
                        section.contains("lore") &&
                        section.contains("interval");
        checkProtected = section ->
                section.contains("name") &&
                        section.contains("material") &&
                        section.contains("ench") &&
                        section.contains("lore") &&
                        section.contains("durability") &&
                        section.contains("durability_decrease");
    }

    public static void load() {
        FileConfiguration config = ApexBattle.getInst().getConfig();
        {
            ConfigurationSection section = config.getConfigurationSection("protected");
            if (checkProtected.test(section)) {
                String name = section.getString("name");
                String material = section.getString("material");
                boolean ench = section.getBoolean("ench");
                List<String> lore = section.getStringList("lore");
                int durability = section.getInt("durability");
                int durability_decrease = section.getInt("durability_decrease");

                Material mat = Material.getMaterial(material);
                if (mat == null) {
                    Logger.log("在加载被保护物品时出现错误！原因：材质类型错误");
                    return;
                } else if (mat == Material.AIR) {
                    Logger.log("在加载被保护物品时出现错误！原因：材质类型不能为空气");
                    return;
                }

                protectedItem = new ProtectedItem(name, mat, ench, lore, durability, durability_decrease);
            } else {
                Logger.log("在加载被保护物品时出现错误！原因：缺少必要节点");
                return;
            }
        }
        ConfigurationSection section = config.getConfigurationSection("resource");
        for (String id : section.getKeys(false)) {
            if (ItemHandler.checkConfig.test(section.getConfigurationSection(id))) {
                String name = section.getString(id + ".name");
                String material = section.getString(id + ".material");
                boolean ench = section.getBoolean(id + ".ench");
                List<String> lore = section.getStringList(id + ".lore");
                long interval = section.getLong(id + ".interval");

                Material mat = Material.getMaterial(material);
                if (mat == null) {
                    Logger.log("在加载资源物品 " + id + " 时出现错误！原因：材质类型错误");
                    continue;
                } else if (mat == Material.AIR) {
                    Logger.log("在加载资源物品 " + id + " 时出现错误！原因：材质类型不能为空气");
                    continue;
                }
                ItemHandler.resource.add(new ItemHandler(id, name, mat, ench, lore, interval));
            } else {
                Logger.log("在加载资源物品 " + id + " 时出现错误！原因：缺少必要节点");
                continue;
            }
        }
    }

    @Getter
    private final String id;
    private final String name;
    private final Material material;
    private final boolean ench;
    private final List<String> lore;
    @Getter
    private final long interval;

    private ItemHandler(final String id, final String name, final Material material, final boolean ench, final List<String> lore, final long interval) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.ench = ench;
        this.lore = lore;
        this.interval = interval;
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
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}