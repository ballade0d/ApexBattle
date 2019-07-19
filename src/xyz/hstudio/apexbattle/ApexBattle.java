package xyz.hstudio.apexbattle;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.apexbattle.config.ConfigManager;
import xyz.hstudio.apexbattle.config.MessageManager;
import xyz.hstudio.apexbattle.game.Game;
import xyz.hstudio.apexbattle.game.Resource;
import xyz.hstudio.apexbattle.game.Sign;
import xyz.hstudio.apexbattle.game.Team;
import xyz.hstudio.apexbattle.listener.EventListener;
import xyz.hstudio.apexbattle.util.AxisAlignedBB;
import xyz.hstudio.apexbattle.util.Logger;
import xyz.hstudio.apexbattle.util.NmsUtil;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

public class ApexBattle extends JavaPlugin {

    @Getter
    private static ApexBattle instance;

    @Getter
    private FileConfiguration config;
    @Getter
    private FileConfiguration message;
    @Getter
    private ConfigManager configManager;
    @Getter
    private MessageManager messageManager;

    public ApexBattle() {
        instance = this;
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        // 判断版本
        NmsUtil.init();
        if (NmsUtil.getInstance() == null) {
            Logger.info("ApexBattle不支持该版本！");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            Logger.info("创建插件文件夹失败！");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        File gameDir = new File(getDataFolder(), "game");
        if (!gameDir.exists() && !gameDir.mkdirs()) {
            Logger.info("创建游戏文件夹失败！");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        File mapDir = new File(getDataFolder(), "map");
        if (!mapDir.exists() && !mapDir.mkdirs()) {
            Logger.info("创建地图文件夹失败！");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // 注册可序列化的类
        ConfigurationSerialization.registerClass(Resource.class, "Resource");
        ConfigurationSerialization.registerClass(Sign.class, "Sign");
        ConfigurationSerialization.registerClass(Team.class, "Team");
        ConfigurationSerialization.registerClass(AxisAlignedBB.class, "AxisAlignedBB");

        // 加载config.yml
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = getResource("xyz/hstudio/apexbattle/config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (Exception e) {
                e.printStackTrace();
                Logger.info("创建配置文件失败！");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);

        // 加载message.yml
        File messageFile = new File(getDataFolder(), "message.yml");
        if (!messageFile.exists()) {
            try (InputStream in = getResource("xyz/hstudio/apexbattle/message.yml")) {
                Files.copy(in, messageFile.toPath());
            } catch (Exception e) {
                e.printStackTrace();
                Logger.info("创建语言文件失败！");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        this.message = YamlConfiguration.loadConfiguration(messageFile);

        this.configManager = new ConfigManager();
        this.messageManager = new MessageManager();
        new ApexCommand();
        new EventListener();

        File[] games = gameDir.listFiles();
        if (games != null) {
            Arrays.stream(games).forEach(Game::new);
        }

        Logger.info("已加载 " + Game.getGames().size() + " 个游戏！");

        long end = System.currentTimeMillis();
        Logger.info("ApexBattle已启动！耗时：" + ((end - start) / 1000D) + "秒");
    }

    @Override
    public void onDisable() {
        Game.getGames().forEach(Game::disable);
    }
}