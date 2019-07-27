package xyz.hstudio.apexbattle.config;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.shop.Item;
import xyz.hstudio.apexbattle.util.NmsUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopManager {

    @Getter
    private String title;
    private int size;
    @Getter
    private Map<ItemStack, List<Item>> shopItems = new HashMap<>();

    public ShopManager() {
        FileConfiguration shop = ApexBattle.getInstance().getShop();
        this.title = shop.getString("title");
        this.size = shop.getInt("size");
        for (String type : shop.getKeys(false)) {
            ItemStack icon = shop.getItemStack(type + ".icon");
            List<Item> goods = (List<Item>) shop.getList(type + ".items");

            this.shopItems.put(NmsUtil.getInstance().addUniqueTag(icon, "Icon"), goods);
        }
    }

    public Inventory create() {
        Inventory inventory = Bukkit.createInventory(null, this.size, this.title);
        for (Map.Entry<ItemStack, List<Item>> entry : this.shopItems.entrySet()) {
            ItemStack icon = entry.getKey();
            if (icon == null) {
                continue;
            }
            inventory.addItem(icon);
        }
        return inventory;
    }

    public Inventory create(final ItemStack icon) {
        if (!this.shopItems.containsKey(icon)) {
            return null;
        }
        List<Item> goods = this.shopItems.get(icon);
        int size = goods.size();
        // 改为9的倍数
        if (size % 9 != 0) {
            size -= size % 9;
            size += 9;
        }
        Inventory inventory = Bukkit.createInventory(null, size, this.title);
        goods.forEach(item -> inventory.addItem(item.getItemStack()));
        return inventory;
    }
}