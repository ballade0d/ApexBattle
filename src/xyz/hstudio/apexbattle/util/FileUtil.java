package xyz.hstudio.apexbattle.util;

import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.game.Game;

import java.io.File;
import java.io.IOException;

public class FileUtil {

    /**
     * 判断游戏文件是否存在
     *
     * @param name 游戏名
     * @return 存在性
     */
    public static boolean isGameExist(final String name) {
        File gameFile = new File(ApexBattle.getInstance().getDataFolder(), "game/" + name + ".yml");
        return gameFile.exists();
    }

    /**
     * 创建游戏文件
     *
     * @param name 游戏名
     * @return 是否成功
     */
    public static boolean createGame(final String name) {
        File gameFile = new File(ApexBattle.getInstance().getDataFolder(), "game/" + name + ".yml");
        try {
            if (gameFile.createNewFile()) {
                Game game = new Game(gameFile);
                game.setName(name);
                return true;
            }
        } catch (IOException ignore) {
        }
        return false;
    }
}