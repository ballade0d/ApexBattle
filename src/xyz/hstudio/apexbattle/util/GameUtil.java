package xyz.hstudio.apexbattle.util;

import org.bukkit.entity.Player;
import xyz.hstudio.apexbattle.game.Game;
import xyz.hstudio.apexbattle.game.GamePlayer;

public class GameUtil {

    public static boolean isGaming(final Player p) {
        for (Game game : Game.getGames()) {
            for (GamePlayer gamePlayer : game.getGamePlayers()) {
                if (gamePlayer.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Game getPlayingGame(final Player p) {
        for (Game game : Game.getGames()) {
            for (GamePlayer gamePlayer : game.getGamePlayers()) {
                if (gamePlayer.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                    return game;
                }
            }
        }
        return null;
    }
}