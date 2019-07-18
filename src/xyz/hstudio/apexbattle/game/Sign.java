package xyz.hstudio.apexbattle.game;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Sign implements ConfigurationSerializable {

    private final World world;
    private final double x;
    private final double y;
    private final double z;

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("world", this.world.getName());
        data.put("x", this.x);
        data.put("y", this.y);
        data.put("z", this.z);
        return data;
    }

    @SuppressWarnings("unused")
    public static Sign deserialize(Map<String, Object> args) {
        World world = Bukkit.getWorld((String) args.get("world"));
        return new Sign(world, (Double) args.get("x"), (Double) args.get("y"), (Double) args.get("z"));
    }
}