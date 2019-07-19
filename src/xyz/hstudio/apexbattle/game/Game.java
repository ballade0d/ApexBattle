package xyz.hstudio.apexbattle.game;

import com.boydti.fawe.bukkit.wrapper.AsyncWorld;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.annotation.LoadFromConfig;
import xyz.hstudio.apexbattle.config.ConfigLoader;
import xyz.hstudio.apexbattle.util.AxisAlignedBB;
import xyz.hstudio.apexbattle.util.WorldUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    private int team_size;

    @LoadFromConfig(path = "min_player")
    @Getter
    @Setter
    private int min_player;

    @LoadFromConfig(path = "max_player")
    @Getter
    @Setter
    private int max_player;

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
    private BukkitTask task;

    private boolean counting = false;
    private int time = ApexBattle.getInstance().getConfigManager().wait_time;

    private long currentTick = 0;

    public Game(final File gameFile) {
        // 加载配置
        FileConfiguration config = YamlConfiguration.loadConfiguration(gameFile);
        ConfigLoader.load(this, config);

        this.region = (AxisAlignedBB) config.get("region");

        this.lobby = (Location) config.get("lobby");

        this.signs = (List<Sign>) config.getList("signs");

        this.teams = (List<Team>) config.getList("teams");

        this.resources = (List<Resource>) config.getList("resources");

        this.statue = isGameAvailable() ? GameStatue.WAITING : GameStatue.STOP;
        gamePlayers = new ArrayList<>();
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                currentTick++;
                switch (getStatue()) {
                    case WAITING: {
                        // 正在倒计时
                        if (counting) {
                            if (hasEnoughPlayer()) {
                                // 拥有足够玩家，继续计时
                                time--;
                                gamePlayers.forEach(gamePlayer -> gamePlayer.setLevel(time));
                                // 时间到，开始游戏
                                if (time <= 0) {
                                    // 让没有队伍的玩家加入队伍
                                    gamePlayers.forEach(gamePlayer -> getLowestTeam().getGamePlayers().add(gamePlayer));
                                    // 队伍传送到队伍出生点
                                    teams.forEach(team -> team.getGamePlayers().forEach(gamePlayer -> gamePlayer.teleport(team.getLocation())));
                                    // 更改游戏状态
                                    statue = GameStatue.GAMING;
                                    // 停止倒计时
                                    counting = false;
                                    // 为游戏结束后计时器做准备
                                    time = 10;
                                }
                            } else {
                                // 发送Title
                                gamePlayers.forEach(gamePlayer -> gamePlayer.sendTitle(ApexBattle.getInstance().getMessageManager().no_enough_player, "", 0, 60, 20));
                                // 设置经验计时器为0
                                gamePlayers.forEach(gamePlayer -> gamePlayer.setLevel(0));
                                // 停止计时
                                counting = false;
                                // 重置时间
                                time = ApexBattle.getInstance().getConfigManager().wait_time;
                            }
                        } else {
                            if (hasEnoughPlayer()) {
                                // 拥有足够玩家，开始计时
                                counting = true;
                            }
                        }
                        break;
                    }
                    case GAMING: {
                        // 生成资源
                        resources.forEach(resource -> {
                            Map.Entry<Integer, ItemStack> entry = resource.get();
                            if (currentTick % entry.getKey() == 0) {
                                resource.getLocation().getWorld().dropItemNaturally(resource.getLocation(), entry.getValue());
                            }
                        });
                        break;
                    }
                    case ENDING: {
                        // 正在计时
                        counting = true;
                        time--;
                        if (time <= 0) {
                            // 玩家退出游戏
                            gamePlayers.forEach(gamePlayer -> removePlayer(gamePlayer.player));
                            // 重置地图
                            reset();
                            // 更新游戏状态
                            statue = GameStatue.STOP;
                        }
                        break;
                    }
                    case STOP: {
                        if (isGameAvailable()) {
                            // 重置地图信息
                            teams.forEach(team -> team.getGamePlayers().clear());
                            gamePlayers.clear();
                            counting = false;
                            time = ApexBattle.getInstance().getConfigManager().wait_time;
                            statue = GameStatue.WAITING;
                            currentTick = 0;
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        }.runTaskTimer(ApexBattle.getInstance(), 20L, 20L);

        games.add(this);
    }

    /**
     * 获取人数最少的队伍
     *
     * @return 人数最少的队伍
     */
    private Team getLowestTeam() {
        Team lowest = null;
        for (Team team : this.teams) {
            if (lowest == null) {
                lowest = team;
                continue;
            }
            if (team.getGamePlayers().size() < lowest.getGamePlayers().size()) {
                lowest = team;
            }
        }
        return lowest;
    }

    /**
     * 判断是否拥有足够玩家来开始游戏
     *
     * @return 结果
     */
    private boolean hasEnoughPlayer() {
        return this.gamePlayers.size() >= this.min_player;
    }

    /**
     * 检查游戏是否可用
     *
     * @return 是否可用
     */
    private boolean isGameAvailable() {
        return Arrays.stream(this.getClass().getFields()).noneMatch(f -> {
            f.setAccessible(true);
            try {
                return !f.getName().equals("signs") && f.get(Game.this) == null;
            } catch (IllegalAccessException ignore) {
                return false;
            }
        }) && region.getWorld() != null && Bukkit.getWorlds().stream().anyMatch(world -> world.getName().equals(region.getWorld().getName()));
    }

    /**
     * 玩家加入游戏
     *
     * @param p 玩家
     */
    public void addPlayer(final Player p) {
        GamePlayer gamePlayer = new GamePlayer(p, p.getLocation(), p.getInventory(), p.getTotalExperience());
        gamePlayer.teleport(this.lobby);
        gamePlayer.getPlayer().getInventory().clear();
        this.gamePlayers.add(gamePlayer);
    }

    /**
     * 处理玩家死亡
     *
     * @param p 玩家
     */
    public void onDeath(final Player p) {

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
        gamePlayer.setTexp(gamePlayer.getExp());
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
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
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
            gamePlayer.setTexp(gamePlayer.exp);
        });
        this.gamePlayers.clear();
        this.reset();
    }

    @RequiredArgsConstructor
    static class GamePlayer {
        @Getter
        private final Player player;
        @Getter
        private final Location prevLoc;
        @Getter
        private final PlayerInventory prevInv;
        @Getter
        private final int exp;
        @Getter
        @Setter
        private Team team;

        /**
         * 传送
         *
         * @param to 地点
         */
        void teleport(final Location to) {
            this.player.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }

        /**
         * 设置背包
         *
         * @param inv 新的背包
         */
        void setInv(final PlayerInventory inv) {
            this.player.getInventory().clear();
            for (int i = 0; i < 40; i++) {
                this.player.getInventory().setItem(i, inv.getItem(i));
            }
        }

        /**
         * 设置经验值
         *
         * @param exp 经验值
         */
        void setTexp(final int exp) {
            this.player.setTotalExperience(exp);
        }

        /**
         * 设置等级
         *
         * @param level 等级
         */
        void setLevel(final int level) {
            this.player.setLevel(level);
        }

        /**
         * 发送Title
         *
         * @param main    主标题
         * @param sub     副标题
         * @param fadeIn  淡入时间
         * @param stay    停留时间
         * @param fadeOut 淡出时间
         */
        void sendTitle(final String main, final String sub, final int fadeIn, final int stay, final int fadeOut) {
            this.player.sendTitle(main, sub, fadeIn, stay, fadeOut);
        }
    }

    public enum GameStatue {
        WAITING, GAMING, ENDING, STOP
    }
}