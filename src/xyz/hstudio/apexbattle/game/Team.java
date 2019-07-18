package xyz.hstudio.apexbattle.game;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Team implements ConfigurationSerializable {

    private final String name;
    private final String color;
    private final Location location;

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        data.put("color", this.color);
        data.put("loc", this.location);
        return data;
    }

    @SuppressWarnings("unused")
    public static Team deserialize(Map<String, Object> args) {
        return new Team((String) args.get("name"), (String) args.get("color"), (Location) args.get("loc"));
    }
}