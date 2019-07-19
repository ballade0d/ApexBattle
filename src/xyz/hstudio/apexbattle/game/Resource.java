package xyz.hstudio.apexbattle.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.apexbattle.ApexBattle;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Resource implements ConfigurationSerializable {

    @Getter
    private final String type;
    @Getter
    private final Location location;

    public Map.Entry<Integer, ItemStack> get() {
        return ApexBattle.getInstance().getConfigManager().resourceMap.get(this.type);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("type", this.type);
        data.put("loc", this.location);
        return data;
    }

    @SuppressWarnings("unused")
    public static Resource deserialize(Map<String, Object> args) {
        return new Resource((String) args.get("type"), (Location) args.get("loc"));
    }
}