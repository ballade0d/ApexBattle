package xyz.hstudio.apexbattle.shop;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.config.ShopManager;
import xyz.hstudio.apexbattle.util.NmsUtil;

import java.util.List;

public class ShopListener implements Listener {

    public ShopListener() {
        Bukkit.getPluginManager().registerEvents(this, ApexBattle.getInstance());
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (e.getItem() == null) {
            return;
        }
        Player p = e.getPlayer();
        ItemStack itemStack = e.getItem();
        if (NmsUtil.getInstance().hasUniqueTag(itemStack, "Shop")) {
            e.setCancelled(true);
            p.openInventory(ApexBattle.getInstance().getShopManager().create());
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        Inventory inventory = e.getClickedInventory();
        ShopManager manager = ApexBattle.getInstance().getShopManager();
        if (inventory == null || inventory.getType() != InventoryType.CHEST || inventory.getTitle() == null || !inventory.getTitle().equals(manager.getTitle())) {
            return;
        }
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        if (NmsUtil.getInstance().hasUniqueTag(clicked, "Icon") && manager.getShopItems().containsKey(clicked)) {
            Inventory next = manager.create(clicked);
            Bukkit.getScheduler().runTask(ApexBattle.getInstance(), () -> p.openInventory(next));
            return;
        }
        Inventory pInv = p.getInventory();
        // 判断是否是商品
        String tag = NmsUtil.getInstance().getUniqueTag(clicked);
        if (tag == null) {
            return;
        }
        // 获取需求和物品
        String[] str = tag.split(",");
        ItemStack required = ApexBattle.getInstance().getConfigManager().resourceMap.get(str[0]).getValue();
        int amount = Integer.parseInt(str[1]);
        ItemStack reward = NmsUtil.getInstance().removeUniqueTag(clicked);
        // 删除售价标签
        ItemMeta rewardMeta = reward.getItemMeta();
        List<String> lore = rewardMeta.getLore();
        lore.remove(lore.size() - 1);
        rewardMeta.setLore(lore);
        reward.setItemMeta(rewardMeta);

        // 是否按Shift，如果是就买一组
        if (e.isShiftClick()) {
            for (int i = 0; i < 64; i++) {
                ItemStack itemStack = getItemStack(required, pInv);
                if (itemStack != null) {
                    itemStack.setAmount(itemStack.getAmount() - amount);
                    pInv.addItem(reward);
                } else {
                    break;
                }
            }
        } else {
            ItemStack itemStack = getItemStack(required, pInv);
            if (itemStack != null) {
                itemStack.setAmount(itemStack.getAmount() - amount);
                pInv.addItem(reward);
            }
        }
    }

    private ItemStack getItemStack(final ItemStack required, final Inventory inv) {
        if (required == null) {
            return null;
        }
        for (ItemStack itemStack : inv.getContents()) {
            if (itemStack != null && required.getType() == itemStack.getType() && required.getAmount() <= itemStack.getAmount()) {
                return itemStack;
            }
        }
        return null;
    }
}