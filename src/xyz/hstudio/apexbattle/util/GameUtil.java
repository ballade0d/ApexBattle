package xyz.hstudio.apexbattle.util;

import org.bukkit.entity.Player;
import xyz.hstudio.apexbattle.game.Game;

public class GameUtil {

    /**
     * 获取玩家正在玩的游戏
     *
     * @param p 玩家
     * @return 正在玩的游戏
     */
    public static Game getGamePlaying(final Player p) {
        return Game.getGames().stream().filter(g -> g.getGamePlayers().stream().anyMatch(gamePlayer -> gamePlayer.getUuid().equals(p.getUniqueId()))).findFirst().orElse(null);
    }

    /**
     * 根据游戏名获取游戏对象
     *
     * @param name 游戏名
     * @return 游戏对象
     */
    public static Game getGame(final String name) {
        return Game.getGames().stream().filter(game -> game.getName().equals(name)).findFirst().orElse(null);
    }
}