package xyz.hstudio.apexbattle.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class Team implements ConfigurationSerializable {

    private final String name;
    @Getter
    private final String color;
    @Getter
    private final Location location;
    @Getter
    @Setter
    private Game.GamePlayer holder;
    @Getter
    @Setter
    private boolean lose = false;
    @Getter
    @Setter
    private int count = 5;
    @Getter
    private List<Game.GamePlayer> gamePlayers = new ArrayList<>();

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        data.put("color", this.color);
        data.put("loc", this.location);
        return data;
    }

    public String getName() {
        return getColor() + name;
    }

    public ChatColor getColor() {
        return ChatColor.valueOf(this.color);
    }

    @SuppressWarnings("unused")
    public static Team deserialize(Map<String, Object> args) {
        return new Team((String) args.get("name"), (String) args.get("color"), (Location) args.get("loc"));
    }
}