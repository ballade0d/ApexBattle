package xyz.hstudio.apexbattle.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.game.internal.ItemHandler;
import xyz.hstudio.apexbattle.util.AABB;
import xyz.hstudio.apexbattle.util.ColorUtil;
import xyz.hstudio.apexbattle.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Game {

    @Getter
    private static final List<Game> games = new ArrayList<>();
    private static final Predicate<ConfigurationSection> checkConfig;
    private static final Predicate<ConfigurationSection> checkRegion;
    private static final Predicate<ConfigurationSection> checkSign;
    private static final Predicate<ConfigurationSection> checkTeam;
    private static final Predicate<ConfigurationSection> checkResource;

    private static List<String> waiting;
    private static List<String> gaming;
    private static List<String> stop;

    static {
        checkConfig = section ->
                section.contains("name") &&
                        section.contains("team_size") &&
                        section.contains("min_player") &&
                        section.contains("max_player") &&
                        section.contains("lobby.world") &&
                        section.contains("lobby.x") &&
                        section.contains("lobby.y") &&
                        section.contains("lobby.z") &&
                        section.contains("lobby.yaw") &&
                        section.contains("lobby.pitch");
        checkRegion = section ->
                section.contains("world") &&
                        section.contains("x1") &&
                        section.contains("y1") &&
                        section.contains("z1") &&
                        section.contains("x2") &&
                        section.contains("y2") &&
                        section.contains("z2");
        checkSign = section ->
                section.contains("world") &&
                        section.contains("x") &&
                        section.contains("y") &&
                        section.contains("z");
        checkTeam = section ->
                section.contains("color") &&
                        section.contains("world") &&
                        section.contains("x") &&
                        section.contains("y") &&
                        section.contains("z") &&
                        section.contains("yaw") &&
                        section.contains("pitch");
        checkResource = section ->
                section.contains("type") &&
                        section.contains("loc.world") &&
                        section.contains("loc.x") &&
                        section.contains("loc.y") &&
                        section.contains("loc.z") &&
                        section.contains("loc.yaw") &&
                        section.contains("loc.pitch");
    }

    public static void load() {
        FileConfiguration conf = ApexBattle.getInst().getConfig();
        waiting = conf.getStringList("sign.waiting");
        gaming = conf.getStringList("sign.gaming");
        stop = conf.getStringList("sign.stop");
        for (FileConfiguration config : ApexBattle.getInst().getGames()) {
            ConfigurationSection section = config.getDefaultSection();
            if (checkConfig.test(section)) {
                String name = section.getString("name");
                int team_size = section.getInt("team_size");
                int min_player = section.getInt("min_player");
                int max_player = section.getInt("max_player");
                World lobbyWorld = Bukkit.getWorld(section.getString("lobby.world"));
                if (lobbyWorld == null) {
                    Logger.log("在加载地图 " + config.getName() + " 时出现错误！原因：大厅出生地不存在");
                    continue;
                }
                AABB aabb = null;
                Location lobby = new Location(lobbyWorld, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"), (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));

                List<SignHandler> signList = new ArrayList<>();
                List<Team> teamList = new ArrayList<>();
                List<Resource> resourceList = new ArrayList<>();

                ConfigurationSection region = section.getConfigurationSection("region");
                if (checkRegion.test(region)) {
                    World world = Bukkit.getWorld(region.getString("world"));
                    if (world == null) {
                        Logger.log("在加载地图 " + config.getName() + " 时出现错误！原因：游戏世界不存在");
                        continue;
                    }
                    double x1 = region.getDouble("x1");
                    double y1 = region.getDouble("y1");
                    double z1 = region.getDouble("z1");
                    double x2 = region.getDouble("x2");
                    double y2 = region.getDouble("y2");
                    double z2 = region.getDouble("z2");
                    aabb = new AABB(x1, y1, z1, x2, y2, z2, world);
                } else {
                    Logger.log("在加载地图 " + config.getName() + " 时出现错误！原因：区域设置缺少必要节点");
                    continue;
                }

                ConfigurationSection signs = section.getConfigurationSection("signs");
                for (String sign : signs.getKeys(false)) {
                    ConfigurationSection signSection = signs.getConfigurationSection(sign);
                    if (checkSign.test(signSection)) {
                        World world = Bukkit.getWorld(signSection.getString("world"));
                        if (world == null) {
                            continue;
                        }
                        double x = signSection.getDouble("x");
                        double y = signSection.getDouble("y");
                        double z = signSection.getDouble("z");

                        signList.add(new SignHandler(world, x, y, z));
                    }
                }

                ConfigurationSection teams = section.getConfigurationSection("teams");
                for (String team : teams.getKeys(false)) {
                    ConfigurationSection teamSection = teams.getConfigurationSection(team);
                    if (checkTeam.test(teamSection)) {
                        Color color = ColorUtil.getColor(teamSection.getString("color"));
                        if (color == null) {
                            Logger.log("在加载地图 " + config.getName() + " 时出现错误！原因：队伍 " + team + " 的颜色错误");
                            continue;
                        }
                        DyeColor dyeColor = DyeColor.getByColor(color);
                        World world = Bukkit.getWorld(teamSection.getString("world"));
                        if (world == null) {
                            Logger.log("在加载地图 " + config.getName() + " 时出现错误！原因：队伍 " + team + " 的出生点不存在");
                            continue;
                        }
                        double x = teamSection.getDouble("x");
                        double y = teamSection.getDouble("y");
                        double z = teamSection.getDouble("z");
                        float yaw = (float) teamSection.getDouble("yaw");
                        float pitch = (float) teamSection.getDouble("pitch");
                        teamList.add(new Team(dyeColor, team, world, x, y, z, yaw, pitch));
                    } else {
                        Logger.log("在加载地图 " + config.getName() + " 时出现错误！原因：队伍 " + team + " 缺少必要节点");
                        continue;
                    }
                }

                ConfigurationSection resources = section.getConfigurationSection("resources");
                for (String resource : teams.getKeys(false)) {
                    ConfigurationSection resourceSection = resources.getConfigurationSection(resource);
                    if (checkResource.test(resourceSection)) {
                        String type = resourceSection.getString("type");
                        if (ItemHandler.resource.stream().noneMatch(res -> res.getId().equalsIgnoreCase(type))) {
                            Logger.log("在加载地图 " + config.getName() + " 时出现错误！原因：资源类型不存在");
                            continue;
                        }
                        World world = Bukkit.getWorld(resourceSection.getString("loc.world"));
                        if (world == null) {
                            Logger.log("在加载地图 " + config.getName() + " 时出现错误！原因：资源 " + resource + " 的出生点不存在");
                            continue;
                        }
                        ItemHandler itemHandler = ItemHandler.resource.stream().filter(res -> res.getId().equals(type)).findFirst().get();
                        double x = resourceSection.getDouble("loc.x");
                        double y = resourceSection.getDouble("loc.y");
                        double z = resourceSection.getDouble("loc.z");
                        float yaw = (float) resourceSection.getDouble("loc.yaw");
                        float pitch = (float) resourceSection.getDouble("loc.pitch");
                        resourceList.add(new Resource(itemHandler, world, x, y, z, yaw, pitch));
                    } else {
                        Logger.log("在加载地图 " + config.getName() + " 时出现错误！原因：资源点配置缺少必要节点");
                        continue;
                    }
                }

                Game.games.add(new Game(name, (short) team_size, min_player, max_player, aabb, lobby, signList, teamList, resourceList, conf.getInt("wait_time")));
            } else {
                Logger.log("在加载地图 " + config.getName() + " 时出现错误！原因：缺少必要节点");
            }
        }
        Logger.log("已加载 " + Game.games.size() + " 个地图！");
    }

    // 游戏基本信息
    private final String name;
    private final short team_size;
    private final int min_player;
    private final int max_player;
    private final AABB aabb;
    private final Location lobby;
    @Getter
    private final List<SignHandler> signs;
    private final List<Team> teams;
    private final List<Resource> resources;
    @Getter
    private final List<GamePlayer> gamePlayers;
    private final int wait_time;

    private int need_time;
    private boolean isCountingDown;

    private long currentTick;

    // 游戏信息
    @Getter
    @Setter
    private GameStatus status = GameStatus.STOP;

    public Game(final String name, final short team_size, final int min_player, final int max_player, final AABB aabb, final Location lobby, final List<SignHandler> signs, final List<Team> teams, final List<Resource> resources, final int wait_time) {
        this.name = name;
        this.team_size = team_size;
        this.min_player = min_player;
        this.max_player = max_player;
        this.aabb = aabb;
        this.lobby = lobby;
        this.signs = signs;
        this.teams = teams;
        this.resources = resources;
        this.gamePlayers = new ArrayList<>();
        this.wait_time = wait_time;

        this.isCountingDown = false;

        init();
    }

    public void onQuit(final Player p) {
        for (int i = 0; i < this.gamePlayers.size(); i++) {
            GamePlayer gp = this.gamePlayers.get(i);
            if (gp.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                gp.teleport(gp.getPrevLoc());
                this.gamePlayers.remove(i);
                break;
            }
        }
        updateSigns();
    }

    public void onJoin(final Player p) {
        GamePlayer newGp = new GamePlayer(p, p.getLocation());
        this.gamePlayers.add(newGp);
        newGp.teleport(this.lobby);
        newGp.getPlayer().setGameMode(GameMode.SURVIVAL);
        updateSigns();
    }

    public GamePlayer getGamePlayer(final Player p) {
        for (GamePlayer gamePlayer : this.gamePlayers) {
            if (gamePlayer.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                return gamePlayer;
            }
        }
        return null;
    }

    private void updateSigns() {
        for (SignHandler signHandler : signs) {
            Block block = signHandler.getLoc().getBlock();
            if (block != null && block.getType().name().contains("SIGN")) {
                Sign sign = (Sign) block.getState();
                List<String> msg = status == GameStatus.WAITING || status == GameStatus.GAMING ? status == GameStatus.WAITING ? waiting : gaming : stop;
                for (int i = 0; i < 4; i++) {
                    sign.setLine(i, msg.get(i)
                            .replace("%name%", this.name)
                            .replace("%online%", String.valueOf(this.gamePlayers.size())));
                }
                sign.update();
            }
        }
    }

    private void init() {
        currentTick = 0;
        Bukkit.getScheduler().runTaskTimer(ApexBattle.getInst(), () -> {
            switch (this.status) {
                case WAITING: {
                    if (this.gamePlayers.size() >= this.min_player && this.teams.stream().allMatch(t -> t.gamePlayers.size() > 0)) {
                        if (this.isCountingDown) {
                            this.need_time--;

                            if (this.need_time < 1) {
                                this.setStatus(GameStatus.GAMING);
                                this.isCountingDown = false;
                                for (Team team : this.teams) {
                                    for (GamePlayer gamePlayer : team.gamePlayers) {
                                        gamePlayer.teleport(team.spawn);
                                    }
                                }
                            } else {
                                for (GamePlayer gamePlayer : this.gamePlayers) {
                                    gamePlayer.sendTitle("§a" + this.need_time);
                                }
                            }
                        } else {
                            this.need_time = this.wait_time;
                            this.isCountingDown = true;
                        }
                    } else {
                        if (this.isCountingDown) {
                            this.isCountingDown = false;
                        }
                    }
                    break;
                }
                case GAMING: {
                    for (Resource resource : this.resources) {
                        if (currentTick % resource.itemHandler.getInterval() == 0) {
                            resource.spawn.getWorld().dropItemNaturally(resource.spawn, resource.itemHandler.createItem());
                        }
                    }
                    break;
                }
                case ENDING: {
                    if (this.isCountingDown) {
                        this.need_time--;
                        if (this.need_time <= 0) {
                            this.isCountingDown = false;
                            this.setStatus(GameStatus.STOP);
                        }
                    } else {
                        this.isCountingDown = true;
                    }
                    break;
                }
                case STOP: {
                    break;
                }
            }
            currentTick++;
        }, 20L, 20L);
    }

    public static class SignHandler {
        @Getter
        private final Location loc;

        public SignHandler(final World world, final double x, final double y, final double z) {
            this.loc = new Location(world, x, y, z);
        }
    }

    public static class Resource {
        private final ItemHandler itemHandler;
        private final Location spawn;

        public Resource(final ItemHandler itemHandler, final World world, final double x, final double y, final double z, final float yaw, final float pitch) {
            this.itemHandler = itemHandler;
            this.spawn = new Location(world, x, y, z, yaw, pitch);
        }
    }

    public static class Team {
        private final Location spawn;
        private final List<GamePlayer> gamePlayers;
        private final DyeColor color;
        private final String name;

        public Team(final DyeColor color, final String name, final World world, final double x, final double y, final double z, final float yaw, final float pitch) {
            this.spawn = new Location(world, x, y, z, yaw, pitch);
            this.gamePlayers = new ArrayList<>();
            this.color = color;
            this.name = name;
        }

        private void addPlayer(final GamePlayer gamePlayer) {
            this.gamePlayers.add(gamePlayer);
        }

        private void removePlayer(final GamePlayer gamePlayer) {
            this.gamePlayers.remove(gamePlayer);
        }

        public void win() {
            for (GamePlayer gamePlayer : this.gamePlayers) {
                gamePlayer.sendTitle("§a你赢了！");
                FireworkEffect effect = FireworkEffect.builder().trail(true).flicker(true).withColor(Color.RED).withFade(Color.ORANGE).with(FireworkEffect.Type.STAR).build();
                Firework fw = gamePlayer.getPlayer().getWorld().spawn(gamePlayer.getPlayer().getLocation(), Firework.class);
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(effect);
                meta.setPower(0);
                fw.setFireworkMeta(meta);
                fw.detonate();
            }
        }
    }

    public enum GameStatus {
        STOP, WAITING, GAMING, ENDING
    }
}