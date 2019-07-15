package xyz.hstudio.apexbattle;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.apexbattle.game.Game;
import xyz.hstudio.apexbattle.game.internal.ItemHandler;
import xyz.hstudio.apexbattle.listener.EventListener;
import xyz.hstudio.apexbattle.util.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public final class ApexBattle extends JavaPlugin {

    @Getter
    private static ApexBattle inst;
    @Getter
    private FileConfiguration config;
    @Getter
    private List<FileConfiguration> games;

    public ApexBattle() {
        inst = this;
    }

    @Override
    public void onLoad() {
        long startTime = System.currentTimeMillis();

        if (!Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].equals("v1_12_R1")) {
            Logger.log("ApexBattle不支持该版本，请使用1.12.2！");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        File folder = this.getDataFolder();
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                Logger.log("创建配置文件夹失败！");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        File config = new File(folder, "config.yml");
        if (!config.exists()) {
            try (InputStream in = getResource("xyz/hstudio/apexbattle/config.yml")) {
                Files.copy(in, config.toPath());
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        this.config = YamlConfiguration.loadConfiguration(config);

        File gameDic = new File(folder, "game");
        if (!gameDic.exists()) {
            if (!gameDic.mkdirs()) {
                Logger.log("创建游戏文件夹失败！");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        File[] gameFiles = gameDic.listFiles();
        if (gameFiles != null) {
            for (File game : gameFiles) {
                this.games.add(YamlConfiguration.loadConfiguration(game));
            }
        }

        ItemHandler.load();
        Game.load();

        // 注册监听器
        new EventListener();
        Bukkit.getPluginCommand("apex").setExecutor(new ApexCommand());

        long finishTime = System.currentTimeMillis();
        Logger.log("ApexBattle(" + this.getDescription().getVersion() + ") 已载入.");
        Logger.log("共耗时 " + ((finishTime - startTime) / 1000D) + " 秒");
    }
}