package xyz.hstudio.apexbattle.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class AxisAlignedBB implements Cloneable, ConfigurationSerializable {

    @Getter
    @Setter
    private Vector min;
    @Getter
    @Setter
    private Vector max;
    @Getter
    @Setter
    private World world;

    public AxisAlignedBB(final Vector min, final Vector max, final World world) {
        this.min = min;
        this.max = max;
        this.world = world;
    }

    public AxisAlignedBB() {
        this(new Vector(0, 0, 0), new Vector(0, 0, 0), null);
    }

    public void translate(final Vector vector) {
        min.add(vector);
        max.add(vector);
    }

    public void translate(final Location location) {
        translate(location.toVector());
    }

    /**
     * 判断是否与另一个碰撞箱重合
     *
     * @param other 另一个碰撞箱
     * @return 是否重合
     */
    public boolean isColliding(final AxisAlignedBB other) {
        if (max.getX() < other.getMin().getX() || min.getX() > other.getMax().getX()) {
            return false;
        }
        if (max.getY() < other.getMin().getY() || min.getY() > other.getMax().getY()) {
            return false;
        }
        return !(max.getZ() < other.getMin().getZ()) && !(min.getZ() > other.getMax().getZ());
    }

    public AxisAlignedBB clone() {
        AxisAlignedBB clone;
        try {
            clone = (AxisAlignedBB) super.clone();
            clone.min = this.min.clone();
            clone.max = this.max.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("world", this.world.getName());
        data.put("min", this.min);
        data.put("max", this.max);
        return data;
    }

    @SuppressWarnings("unused")
    public static AxisAlignedBB deserialize(Map<String, Object> args) {
        World world = Bukkit.getWorld((String) args.get("world"));
        return new AxisAlignedBB((Vector) args.get("min"), (Vector) args.get("max"), world);
    }
}