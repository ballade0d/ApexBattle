package xyz.hstudio.apexbattle.game;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GamePlayer {

    @Getter
    private final Player player;
    @Getter
    private final Location prevLoc;
    @Getter
    private Game.Team team;

    GamePlayer(final Player player, final Location prevLoc) {
        this.player = player;
        this.prevLoc = prevLoc;
    }

    void sendTitle(final String msg) {
        this.player.sendTitle(msg, "", 0, 40, 0);
    }

    void teleport(final Location location) {
        this.player.teleport(location);
    }
}