package xyz.hstudio.apexbattle.game;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.DamageSource;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.hstudio.apexbattle.util.AABB;

public class GamePlayer {

    @Getter
    private final Player player;
    @Getter
    private final Location prevLoc;
    @Getter
    @Setter
    private Game.Team team;

    GamePlayer(final Player player, final Location prevLoc) {
        this.player = player;
        this.prevLoc = prevLoc;
    }

    public void sendTitle(final String msg) {
        this.player.sendTitle(msg, "", 0, 40, 0);
    }

    void teleport(final Location location) {
        this.player.teleport(location);
    }

    void damage(final DamageSource damageSource, final float damage) {
        ((CraftPlayer) this.player).getHandle().damageEntity(damageSource, damage);
    }

    boolean isColliding(final AABB other) {
        return AABB.playerAABB.clone().translate(this.player.getLocation().toVector()).isColliding(other);
    }
}