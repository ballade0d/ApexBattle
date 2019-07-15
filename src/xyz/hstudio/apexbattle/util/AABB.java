package xyz.hstudio.apexbattle.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class AABB implements Cloneable {

    public static final AABB playerAABB = new AABB(new Vector(-0.3, 0, -0.3), new Vector(0.3, 1.8, 0.3), null);

    @Getter
    @Setter
    private Vector min;
    @Getter
    @Setter
    private Vector max;
    @Getter
    @Setter
    private World world;

    public AABB(final Vector min, final Vector max, final World world) {
        this.min = min;
        this.max = max;
        this.world = world;
    }

    public AABB(final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final World world) {
        this(new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ), world);
    }

    public boolean isColliding(final AABB other) {
        if (max.getX() < other.getMin().getX() || min.getX() > other.getMax().getX()) {
            return false;
        }
        if (max.getY() < other.getMin().getY() || min.getY() > other.getMax().getY()) {
            return false;
        }
        return !(max.getZ() < other.getMin().getZ()) && !(min.getZ() > other.getMax().getZ());
    }

    public AABB translate(final Vector vector) {
        min.add(vector);
        max.add(vector);
        return this;
    }

    public AABB clone() {
        AABB clone;
        try {
            clone = (AABB) super.clone();
            clone.min = this.min.clone();
            clone.max = this.max.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}