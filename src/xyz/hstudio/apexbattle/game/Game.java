package xyz.hstudio.apexbattle.game;

import com.boydti.fawe.bukkit.wrapper.AsyncWorld;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.annotation.LoadFromConfig;
import xyz.hstudio.apexbattle.config.ConfigLoader;
import xyz.hstudio.apexbattle.config.YamlConfig;
import xyz.hstudio.apexbattle.util.AxisAlignedBB;
import xyz.hstudio.apexbattle.util.WorldUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Game {

    @Getter
    private static final List<Game> games = new ArrayList<>();

    @LoadFromConfig(path = "name")
    @Getter
    @Setter
    private String name;

    @LoadFromConfig(path = "team_size")
    @Getter
    @Setter
    private String team_size;

    @LoadFromConfig(path = "min_player")
    @Getter
    @Setter
    private String min_player;

    @LoadFromConfig(path = "max_player")
    @Getter
    @Setter
    private String max_player;

    @Getter
    @Setter
    private AxisAlignedBB region;

    @Getter
    @Setter
    private Location lobby;

    @Getter
    @Setter
    private List<Sign> signs;

    @Getter
    @Setter
    private List<Team> teams;

    @Getter
    @Setter
    private List<Resource> resources;

    @Getter
    @Setter
    private GameStatue statue;
    @Getter
    private List<GamePlayer> gamePlayers;
    @Getter
    private Map<Team, List<Guardian>> guardians;
    @Getter
    private BukkitTask task;

    public Game(final File gameFile) {
        // 加载配置
        FileConfiguration config = YamlConfig.loadConfiguration(gameFile);
        ConfigLoader.load(this, config);

        this.region = (AxisAlignedBB) config.get("region");

        this.lobby = (Location) config.get("lobby");

        this.signs = (List<Sign>) config.getList("signs");

        this.teams = (List<Team>) config.getList("teams");

        this.resources = (List<Resource>) config.getList("resources");

        this.statue = Arrays.stream(this.getClass().getFields()).anyMatch(f -> {
            f.setAccessible(true);
            try {
                return !f.getName().equals("signs") && f.get(Game.this) == null;
            } catch (IllegalAccessException ignore) {
                return false;
            }
        }) ? GameStatue.STOP : GameStatue.WAITING;
        gamePlayers = new ArrayList<>();
        guardians = new HashMap<>();

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                // TODO: Implement
            }
        }.runTaskTimer(ApexBattle.getInstance(), 20L, 20L);

        games.add(this);
    }

    /**
     * 玩家加入游戏
     *
     * @param p 玩家
     */
    public void addPlayer(final Player p) {
        GamePlayer gamePlayer = new GamePlayer(p, p.getLocation(), p.getInventory());
        gamePlayer.teleport(this.lobby);
        gamePlayer.getPlayer().getInventory().clear();
        this.gamePlayers.add(gamePlayer);
    }

    /**
     * 玩家退出游戏
     *
     * @param p 玩家
     */
    public void removePlayer(final Player p) {
        GamePlayer gamePlayer = this.gamePlayers.stream().filter(player -> player.getPlayer().getUniqueId().equals(p.getUniqueId())).findFirst().orElse(null);
        if (gamePlayer == null) {
            return;
        }
        gamePlayer.teleport(gamePlayer.getPrevLoc());
        gamePlayer.setInv(gamePlayer.getPrevInv());
        this.gamePlayers.remove(gamePlayer);
    }

    /**
     * 重置地图
     */
    private void reset() {
        Bukkit.unloadWorld(this.region.getWorld(), false);
        WorldUtil.resetWorld(this.region.getWorld(), this.name);
        WorldCreator worldCreator = new WorldCreator(this.region.getWorld().getName());
        AsyncWorld asyncWorld = AsyncWorld.create(worldCreator);
        asyncWorld.commit();
    }

    /**
     * 保存游戏信息到文件
     *
     * @param file 文件
     * @return 是否成功
     */
    public boolean saveToFile(final File file) {
        try {
            if (!file.exists() && !file.createNewFile()) {
                return false;
            }
            FileConfiguration config = YamlConfig.loadConfiguration(file);
            config.set("name", this.name);
            config.set("team_size", this.team_size);
            config.set("min_player", this.min_player);
            config.set("max_player", this.max_player);
            config.set("region", this.region);
            config.set("lobby", this.lobby);
            config.set("resources", this.resources);
            config.set("signs", this.signs);
            config.set("teams", this.teams);
            config.save(file);
            return true;
        } catch (IOException ignore) {
            return false;
        }
    }

    /**
     * 卸载
     */
    public void disable() {
        this.task.cancel();
        this.gamePlayers.forEach(gamePlayer -> {
            gamePlayer.teleport(gamePlayer.getPrevLoc());
            gamePlayer.setInv(gamePlayer.getPrevInv());
        });
        this.gamePlayers.clear();
        this.reset();
    }

    @RequiredArgsConstructor
    public static class GamePlayer {
        @Getter
        private final Player player;
        @Getter
        private final Location prevLoc;
        @Getter
        private final PlayerInventory prevInv;
        @Getter
        @Setter
        private Team team;

        /**
         * 传送
         *
         * @param to 地点
         */
        public void teleport(final Location to) {
            this.player.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }

        /**
         * 设置背包
         *
         * @param inv 新的背包
         */
        public void setInv(final PlayerInventory inv) {
            this.player.getInventory().clear();
            for (int i = 0; i < 40; i++) {
                this.player.getInventory().setItem(i, inv.getItem(i));
            }
        }
    }

    public enum GameStatue {
        WAITING, GAMING, ENDING, STOP
    }
}