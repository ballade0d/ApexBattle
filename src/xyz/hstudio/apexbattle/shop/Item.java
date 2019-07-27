package xyz.hstudio.apexbattle.shop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.util.NmsUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Item implements ConfigurationSerializable {

    @Getter
    private final String required;
    @Getter
    private final int amount;
    @Getter
    private final ItemStack itemStack;

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("required", this.required + "," + amount);
        data.put("reward", this.itemStack);
        return data;
    }

    @SuppressWarnings("unused")
    public static Item deserialize(Map<String, Object> args) {
        String[] requiredStr = ((String) args.get("required")).split(",");
        String required = requiredStr[0];
        int amount = Integer.parseInt(requiredStr[1]);
        ItemStack reward = (ItemStack) args.get("reward");
        // 添加价格标签
        ItemMeta rewardMeta = reward.getItemMeta();
        rewardMeta.setLore(Collections.singletonList(ApexBattle.getInstance().getMessageManager().shop_tag
                .replace("%type%", ApexBattle.getInstance().getConfigManager().resourceMap.get(required).getValue().getItemMeta().getDisplayName())
                .replace("%count%", String.valueOf(amount))));
        reward.setItemMeta(rewardMeta);

        return new Item(required, amount, NmsUtil.getInstance().addUniqueTag(reward, required + "," + amount));
    }
}