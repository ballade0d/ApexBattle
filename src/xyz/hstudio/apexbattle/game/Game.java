package xyz.hstudio.apexbattle.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.annotation.LoadFromConfig;
import xyz.hstudio.apexbattle.config.ConfigLoader;
import xyz.hstudio.apexbattle.config.ConfigManager;
import xyz.hstudio.apexbattle.config.MessageManager;
import xyz.hstudio.apexbattle.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Game {

    @Getter
    private static final List<Game> games = new ArrayList<>();

    @LoadFromConfig(path = "name")
    @Getter
    @Setter
    private String name;

    @LoadFromConfig(path = "min_player")
    @Getter
    @Setter
    private int min_player;

    @LoadFromConfig(path = "max_player")
    @Getter
    @Setter
    private int max_player;

    @LoadFromConfig(path = "region")
    @Getter
    @Setter
    private AxisAlignedBB region;

    @LoadFromConfig(path = "lobby")
    @Getter
    @Setter
    private Location lobby;

    @LoadFromConfig(path = "spectate")
    @Getter
    @Setter
    private Location spectate;

    @LoadFromConfig(path = "signs")
    @Getter
    @Setter
    private List<Sign> signs;

    @LoadFromConfig(path = "teams")
    @Getter
    @Setter
    private List<Team> teams;

    @LoadFromConfig(path = "resources")
    @Getter
    @Setter
    private List<Resource> resources;

    @Getter
    private File gameFile;

    @Getter
    @Setter
    private GameStatue statue;
    @Getter
    private List<GamePlayer> gamePlayers;
    @Getter
    private Map<UUID, GamePlayer> lostPlayer;
    @Getter
    private BukkitTask task;
    @Getter
    private Scoreboard scoreboard;

    private boolean counting = false;
    private int time = ApexBattle.getInstance().getConfigManager().wait_time;
    @Getter
    private boolean godMode = true;

    @Getter
    private long currentTick = 0;

    public Game(final File gameFile) {
        // 加载配置
        FileConfiguration config = YamlConfiguration.loadConfiguration(gameFile);
        ConfigLoader.load(this, config);

        this.gameFile = gameFile;

        this.statue = GameStatue.STOP;
        this.gamePlayers = new ArrayList<>();
        this.lostPlayer = new HashMap<>();
        this.task = new Task().runTaskTimer(ApexBattle.getInstance(), 20L, 20L);

        games.add(this);
    }

    /**
     * 更新牌子
     */
    private void updateSign() {
        if (this.getSigns() == null) {
            return;
        }
        for (Sign sign : this.getSigns()) {
            Block block = sign.getWorld().getBlockAt(sign.getX(), sign.getY(), sign.getZ());
            if (block == null || !block.getType().name().contains("SIGN")) {
                continue;
            }
            ConfigManager manager = ApexBattle.getInstance().getConfigManager();
            org.bukkit.block.Sign s = (org.bukkit.block.Sign) block.getState();
            List<String> lines = new ArrayList<>();
            lines.add(manager.sign_first);
            if (this.statue == GameStatue.WAITING) {
                lines.addAll(manager.sign_waiting);
            } else if (this.statue == GameStatue.GAMING) {
                lines.addAll(manager.sign_gaming);
            } else {
                lines.addAll(manager.sign_stop);
            }
            for (int i = 0; i < 4; i++) {
                s.setLine(i, lines.get(i)
                        .replace("%name%", this.getName())
                        .replace("%online%", String.valueOf(this.gamePlayers.size())));
            }
            s.update(true);
        }
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
        return Arrays.stream(this.getClass().getDeclaredFields()).noneMatch(f -> {
            f.setAccessible(true);
            try {
                return !f.getName().equals("signs") && f.get(Game.this) == null;
            } catch (IllegalAccessException ignore) {
                return false;
            }
        }) && region != null && region.getWorld() != null && Bukkit.getWorlds().stream().anyMatch(world -> world.getName().equals(region.getWorld().getName()));
    }

    /**
     * 处理受保护玩家死亡
     *
     * @param team 对应的队伍
     */
    private void death(final Team team, final int count) {
        String main = ApexBattle.getInstance().getMessageManager().player_death_title.replace("%count%", String.valueOf(count));
        String sub = ApexBattle.getInstance().getMessageManager().player_death_sub_title.replace("%count%", String.valueOf(count));
        team.getGamePlayers().forEach(gamePlayer -> gamePlayer.sendTitle(main, sub, 20, 40, 20));
    }

    /**
     * 处理队伍失败
     *
     * @param team 对应的队伍
     */
    private void lose(final Team team) {
        team.setLose(true);
        String main = ApexBattle.getInstance().getMessageManager().lose_title;
        String sub = ApexBattle.getInstance().getMessageManager().lose_sub_title;
        team.getGamePlayers().forEach(gamePlayer -> {
            gamePlayer.setGameMode(GameMode.SPECTATOR);
            gamePlayer.teleport(this.spectate);
            gamePlayer.sendTitle(main, sub, 0, 40, 0);
        });

        List<Team> aliveTeams = this.teams.stream().filter(t -> !t.isLose()).collect(Collectors.toList());
        if (aliveTeams.size() == 1) {
            win(aliveTeams.get(0));
        } else if (aliveTeams.isEmpty()) {
            this.statue = GameStatue.ENDING;
        }
    }

    /**
     * 处理队伍获胜
     *
     * @param team 对应的队伍
     */
    private void win(final Team team) {
        String main = ApexBattle.getInstance().getMessageManager().win_title.replace("%team%", team.getName());
        String sub = ApexBattle.getInstance().getMessageManager().win_sub_title.replace("%team%", team.getName());
        // 按照杀敌数排列
        List<GamePlayer> players = new ArrayList<>(this.gamePlayers);
        players.sort((p1, p2) -> p2.getKillCount() - p1.getKillCount());
        // 发送Title，消息
        String name1 = players.size() >= 1 ? players.get(0).getPlayer().getName() : "无";
        String name2 = players.size() >= 2 ? players.get(1).getPlayer().getName() : "无";
        String name3 = players.size() >= 3 ? players.get(2).getPlayer().getName() : "无";
        this.gamePlayers.forEach(gamePlayer -> {
            gamePlayer.sendTitle(main, sub, 10, 60, 10);
            ApexBattle.getInstance().getMessageManager().end_message.forEach(s -> {
                String msg = s
                        .replace("%team%", team.getName())
                        .replace("%killer_1%", name1)
                        .replace("%killer_2%", name2)
                        .replace("%killer_3%", name3);
                gamePlayer.getPlayer().sendMessage(msg);
            });
        });
        this.statue = GameStatue.ENDING;
    }

    /**
     * 重置地图
     */
    private void reset(final boolean reloadWorld) {
        if (this.region != null && this.region.getWorld() != null) {
            this.region.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof Item) {
                    entity.remove();
                }
            });
            Bukkit.unloadWorld(this.region.getWorld(), false);
            WorldUtil.resetWorld(this.region.getWorld(), this.name);
            if (reloadWorld) {
                WorldCreator worldCreator = new WorldCreator(this.region.getWorld().getName());
                worldCreator.createWorld();
            }
        }
    }

    /**
     * 玩家加入游戏
     *
     * @param p 玩家
     */
    public void addPlayer(final Player p) {
        if (this.lostPlayer.containsKey(p.getUniqueId())) {
            GamePlayer gamePlayer = this.lostPlayer.get(p.getUniqueId());
            p.getInventory().clear();
            gamePlayer.teleport(gamePlayer.getTeam().getLocation());
            gamePlayer.setInv(gamePlayer.getQuitInv());
            this.gamePlayers.add(gamePlayer);
            Team team = gamePlayer.getTeam();
            if (team.isLose()) {
                gamePlayer.teleport(this.spectate);
                p.getInventory().clear();
            }
        } else {
            ExperienceManager manager = new ExperienceManager(p);
            GamePlayer gamePlayer = new GamePlayer(p.getUniqueId(), p.getLocation());
            gamePlayer.teleport(this.lobby);
            manager.setExp(0);
            gamePlayer.setGameMode(GameMode.SURVIVAL);
            gamePlayer.getPlayer().getInventory().clear();
            gamePlayer.getPlayer().getActivePotionEffects().forEach(potionEffect -> gamePlayer.getPlayer().removePotionEffect(potionEffect.getType()));
            gamePlayer.getPlayer().setScoreboard(getScoreboard());
            this.gamePlayers.add(gamePlayer);
        }
    }

    /**
     * 玩家退出游戏
     *
     * @param p 玩家
     */
    public void removePlayer(final Player p) {
        GamePlayer gamePlayer = this.gamePlayers.stream().filter(player -> player.getUuid().equals(p.getUniqueId())).findFirst().orElse(null);
        if (gamePlayer == null) {
            return;
        }
        if (this.statue == GameStatue.GAMING) {
            gamePlayer.setQuitInv(p.getInventory());
            this.lostPlayer.put(p.getUniqueId(), gamePlayer);
        }
        try {
            p.teleport(gamePlayer.getPrevLoc());
            p.getInventory().clear();
            p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        } catch (Exception ignore) {
        } finally {
            this.gamePlayers.remove(gamePlayer);
        }
        if (this.gamePlayers.isEmpty() && this.statue == GameStatue.GAMING) {
            this.statue = GameStatue.ENDING;
        }
        if (this.statue == GameStatue.GAMING) {
            for (Team team : this.teams) {
                team.getGamePlayers().remove(gamePlayer);
                if (team.getGamePlayers().isEmpty() && !team.isLose()) {
                    lose(team);
                }
            }
        }
    }

    /**
     * 根据玩家获取GamePlayer实例
     *
     * @param p 玩家
     * @return GamePlayer实例
     */
    public GamePlayer getGamePlayer(final Player p) {
        return this.gamePlayers.stream().filter(gamePlayer -> gamePlayer.getPlayer().getUniqueId().equals(p.getUniqueId())).findFirst().orElse(null);
    }

    /**
     * 处理玩家死亡
     *
     * @param p 玩家
     */
    public void onDeath(final Player p) {
        // 是物品持有者
        Team team = teams.stream().filter(t -> t.getHolder().getUuid().equals(p.getUniqueId())).findFirst().orElse(null);
        if (team == null) {
            return;
        }
        for (ItemStack itemStack : p.getInventory()) {
            // 判断是否为对应物品
            if (NmsUtil.getInstance().hasUniqueTag(itemStack, "Protected")) {
                if (team.getCount() - 1 <= 0) {
                    p.getInventory().remove(itemStack);
                    lose(team);
                } else {
                    team.setCount(team.getCount() - 1);
                    death(team, team.getCount());
                }
                break;
            }
        }
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
            config.set("min_player", this.min_player);
            config.set("max_player", this.max_player);
            config.set("region", this.region);
            config.set("lobby", this.lobby);
            config.set("spectate", this.spectate);
            config.set("resources", this.resources);
            config.set("signs", this.signs);
            config.set("teams", this.teams);
            config.save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
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
            gamePlayer.getPlayer().getActivePotionEffects().forEach(potionEffect -> gamePlayer.getPlayer().removePotionEffect(potionEffect.getType()));
            gamePlayer.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        });
        this.gamePlayers.clear();
        this.reset(false);
    }

    @RequiredArgsConstructor
    public static class GamePlayer {
        @Getter
        private final UUID uuid;
        @Getter
        private final Location prevLoc;
        @Getter
        @Setter
        private PlayerInventory quitInv;
        @Getter
        @Setter
        private Team team;
        @Getter
        private int killCount;

        public void increaseKillCount() {
            killCount++;
        }

        public Player getPlayer() {
            return Bukkit.getPlayer(this.uuid);
        }

        /**
         * 传送
         *
         * @param to 地点
         */
        void teleport(final Location to) {
            this.getPlayer().teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }

        /**
         * 设置背包
         *
         * @param inv 新的背包
         */
        void setInv(final PlayerInventory inv) {
            this.getPlayer().getInventory().clear();
            for (int i = 0; i < 40; i++) {
                this.getPlayer().getInventory().setItem(i, inv.getItem(i));
            }
        }

        /**
         * 设置游戏模式
         *
         * @param gameMode 游戏模式
         */
        void setGameMode(final GameMode gameMode) {
            this.getPlayer().setGameMode(gameMode);
        }

        /**
         * 设置等级
         *
         * @param level 等级
         */
        void setLevel(final int level) {
            this.getPlayer().setLevel(level);
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
            this.getPlayer().sendTitle(main, sub, fadeIn, stay, fadeOut);
        }
    }

    public enum GameStatue {
        WAITING, GAMING, ENDING, STOP
    }

    private class Task extends BukkitRunnable {
        @Override
        public void run() {
            currentTick++;
            if (region != null && region.getWorld() != null) {
                region.getWorld().setGameRuleValue("doMobSpawning", "false");
                region.getWorld().setGameRuleValue("doDaylightCycle", "false");
                region.getWorld().setGameRuleValue("doWeatherCycle", "false");
            }
            switch (getStatue()) {
                case WAITING: {
                    // 正在倒计时
                    if (counting) {
                        if (hasEnoughPlayer()) {
                            // 拥有足够玩家，继续计时
                            time--;
                            gamePlayers.forEach(gamePlayer -> {
                                gamePlayer.setLevel(time);
                                if (time <= 10) {
                                    gamePlayer.sendTitle("§a" + time, "", 10, 40, 10);
                                }
                            });
                            // 时间到，开始游戏
                            if (time <= 0) {
                                currentTick = 0;
                                // 让没有队伍的玩家加入队伍
                                gamePlayers.forEach(gamePlayer -> {
                                    Team lowest = getLowestTeam();
                                    lowest.getGamePlayers().add(gamePlayer);
                                    gamePlayer.setTeam(lowest);
                                    gamePlayer.getPlayer().setCustomName(lowest.getColor() + "[" + lowest.getName() + "] " + gamePlayer.getPlayer().getName());
                                });
                                // 队伍传送到队伍出生点
                                teams.forEach(team -> team.getGamePlayers().forEach(gamePlayer -> gamePlayer.teleport(team.getLocation())));
                                // 更改游戏状态
                                statue = GameStatue.GAMING;
                                // 停止倒计时
                                counting = false;

                                // 给予物品
                                ItemStack item = ApexBattle.getInstance().getConfigManager().protect;
                                String main = ApexBattle.getInstance().getMessageManager().player_get_item_title;
                                String sub = ApexBattle.getInstance().getMessageManager().player_get_item_sub_title;
                                String noticeMain = ApexBattle.getInstance().getMessageManager().notice_get_item_title;
                                String noticeSub = ApexBattle.getInstance().getMessageManager().notice_get_item_sub_title;
                                ItemStack[] tools = ItemUtil.getNormalTools();
                                ItemStack beef = new ItemStack(Material.COOKED_BEEF, 5);
                                teams.forEach(team -> {
                                    if (team.getGamePlayers().size() > 0) {
                                        GamePlayer gamePlayer = team.getGamePlayers().get(RandomUtil.nextInt(team.getGamePlayers().size()));
                                        gamePlayer.getPlayer().getInventory().setItem(9, item);
                                        gamePlayer.sendTitle(main, sub, 10, 40, 10);
                                        team.setHolder(gamePlayer);
                                        ItemStack[] items = ItemUtil.getNormalItems(ColorUtil.getColor(team.getColor()));
                                        team.getGamePlayers().forEach(gp -> {
                                            gp.getPlayer().getInventory().setHelmet(items[0]);
                                            gp.getPlayer().getInventory().setChestplate(items[1]);
                                            gp.getPlayer().getInventory().setLeggings(items[2]);
                                            gp.getPlayer().getInventory().setBoots(items[3]);
                                            gp.getPlayer().getInventory().addItem(tools);
                                            gp.getPlayer().getInventory().addItem(beef);
                                            gp.getPlayer().getInventory().addItem(ApexBattle.getInstance().getConfigManager().shop);
                                            if (gp != gamePlayer) {
                                                gp.sendTitle(noticeMain.replace("%player%", gamePlayer.getPlayer().getName()), noticeSub.replace("%player%", gamePlayer.getPlayer().getName()), 10, 40, 10);
                                            }
                                        });
                                    } else {
                                        team.setLose(true);
                                    }
                                });
                            }
                        } else {
                            // 发送Title
                            String main = ApexBattle.getInstance().getMessageManager().no_enough_player_title;
                            String sub = ApexBattle.getInstance().getMessageManager().no_enough_player_sub_title;
                            gamePlayers.forEach(gamePlayer -> gamePlayer.sendTitle(main, sub, 0, 60, 20));
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
                    ConfigManager configManager = ApexBattle.getInstance().getConfigManager();

                    // 更新计分板
                    Objective o = scoreboard.getObjective("Info");
                    List<String> lines = new ArrayList<>();
                    for (String line : configManager.scoreboard_lines) {
                        if (line.contains("%team_count%")) {
                            for (Team t : teams) {
                                lines.add(configManager.scoreboard_team_count_format
                                        .replace("%name%", t.getName())
                                        .replace("%count%", t.isLose() ? configManager.scoreboard_lose_symbol : String.valueOf(t.getCount())));
                            }
                        } else {
                            lines.add(line
                                    .replace("%map%", getName())
                                    .replace("%time%", String.valueOf(currentTick)));
                        }
                    }
                    for (int i = 0; i < lines.size(); i++) {
                        org.bukkit.scoreboard.Team team = scoreboard.getTeam("line" + i);
                        o.getScore(ChatColor.values()[i].toString()).setScore(lines.size() - 1 - i);
                        team.setPrefix(lines.get(i));
                    }

                    // 指南针
                    for (GamePlayer gp : gamePlayers) {
                        List<GamePlayer> list = getGamePlayers().stream().filter(gamePlayer -> gamePlayer != gp && gamePlayer.getTeam() != gp.getTeam()).sorted((Comparator.comparingDouble(gamePlayer -> gamePlayer.getPlayer().getLocation().distance(gp.getPlayer().getLocation())))).collect(Collectors.toList());
                        if (list.size() > 0) {
                            gp.getPlayer().setCompassTarget(list.get(0).getPlayer().getLocation());
                        }
                    }
                    MessageManager manager = ApexBattle.getInstance().getMessageManager();
                    String main = manager.weak_title.replace("%time%", String.valueOf(currentTick));
                    String sub = manager.weak_sub_title.replace("%time%", String.valueOf(currentTick));
                    if (currentTick == 240) {
                        String god_main = manager.god_title.replace("%time%", "1");
                        String god_sub = manager.god_sub_title.replace("%time%", "1");
                        gamePlayers.forEach(gamePlayer -> gamePlayer.sendTitle(god_main, god_sub, 10, 40, 10));
                    } else if (currentTick == 300) {
                        godMode = false;
                        String god_end_main = manager.god_end_title;
                        String god_end_sub = manager.god_end_sub_title;
                        gamePlayers.forEach(gamePlayer -> gamePlayer.sendTitle(god_end_main, god_end_sub, 10, 40, 10));
                    } else if (currentTick == 600) {
                        for (Team team : teams) {
                            if (team.getHolder() == null) {
                                continue;
                            }
                            team.getHolder().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100000, 0), true);
                            team.getGamePlayers().forEach(gamePlayer -> gamePlayer.sendTitle(main, sub.replace("%level%", String.valueOf(1)), 10, 40, 10));
                        }
                    }
                    break;
                }
                case ENDING: {
                    // 计时
                    godMode = true;
                    if (!counting) {
                        time = 10;
                    }
                    counting = true;
                    time--;
                    if (time <= 0) {
                        // 玩家退出游戏
                        gamePlayers.forEach(gamePlayer -> {
                            gamePlayer.teleport(gamePlayer.getPrevLoc());
                            gamePlayer.getPlayer().getActivePotionEffects().forEach(potionEffect -> gamePlayer.getPlayer().removePotionEffect(potionEffect.getType()));
                            gamePlayer.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                        });
                        gamePlayers.clear();
                        // 重置地图
                        reset(true);
                        // 更新游戏状态
                        statue = GameStatue.STOP;

                        ConfigLoader.load(Game.this, YamlConfiguration.loadConfiguration(gameFile));
                    }
                    break;
                }
                case STOP: {
                    ConfigManager manager = ApexBattle.getInstance().getConfigManager();
                    scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                    Objective o = scoreboard.registerNewObjective("Info", "dummy");
                    o.setDisplaySlot(DisplaySlot.SIDEBAR);
                    o.setDisplayName(manager.scoreboard_title);
                    List<String> lines = new ArrayList<>();
                    for (String line : manager.scoreboard_lines) {
                        if (line.contains("%team_count%")) {
                            for (Team t : teams) {
                                lines.add(manager.scoreboard_team_count_format
                                        .replace("%name%", t.getName())
                                        .replace("%count%", String.valueOf(manager.protected_durability)));
                            }
                        } else {
                            lines.add(line
                                    .replace("%map%", getName())
                                    .replace("%time%", "0"));
                        }
                    }
                    for (int i = 0; i < lines.size(); i++) {
                        org.bukkit.scoreboard.Team newTeam = scoreboard.registerNewTeam("line" + i);
                        newTeam.addEntry(ChatColor.values()[i].toString());
                        o.getScore(ChatColor.values()[i].toString()).setScore(lines.size() - 1 - i);
                        newTeam.setPrefix(lines.get(i));
                    }

                    if (isGameAvailable()) {
                        // 重置地图信息
                        teams.forEach(team -> {
                            team.setHolder(null);
                            team.setLose(false);
                            team.getGamePlayers().clear();
                            team.setCount(manager.protected_durability);
                        });
                        gamePlayers.clear();
                        lostPlayer.clear();
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
            updateSign();
        }
    }
}