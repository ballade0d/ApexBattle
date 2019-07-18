package xyz.hstudio.apexbattle;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.hstudio.apexbattle.annotation.Cmd;
import xyz.hstudio.apexbattle.config.MessageManager;
import xyz.hstudio.apexbattle.game.Game;
import xyz.hstudio.apexbattle.game.Resource;
import xyz.hstudio.apexbattle.game.Team;
import xyz.hstudio.apexbattle.util.AxisAlignedBB;
import xyz.hstudio.apexbattle.util.FileUtil;
import xyz.hstudio.apexbattle.util.GameUtil;
import xyz.hstudio.apexbattle.util.WorldUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

public class ApexCommand implements CommandExecutor {

    private static final Map<String, Map.Entry<Cmd, Method>> commandMap = new HashMap<>();
    private static ApexCommand instance;

    ApexCommand() {
        Cmd annotation;
        for (Method method : this.getClass().getDeclaredMethods()) {
            // 获取方法的注解
            annotation = method.getAnnotation(Cmd.class);
            if (annotation == null) {
                continue;
            }
            // 使方法可以被访问
            method.setAccessible(true);
            // 获取指令名
            String name = annotation.name();
            // 注册
            commandMap.put(name, new AbstractMap.SimpleEntry<>(annotation, method));
        }
        instance = this;
        Bukkit.getPluginCommand("apex").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(cmd.getName()).append(" ");
        for (String arg : args) {
            sb.append(arg).append(" ");
        }
        onCommand(sender, sb.toString().trim());
        return true;
    }

    private void onCommand(final CommandSender sender, final String cmd) {
        MessageManager manager = ApexBattle.getInstance().getMessageManager();
        if (cmd.equalsIgnoreCase("apex")) {
            sender.sendMessage(manager.prefix + "ApexBattle by MrCraftGoo");
            return;
        }
        String[] args = cmd.split(" ");
        String main = args[0];
        // 是插件的指令
        if (main.equalsIgnoreCase("apex")) {
            // 获取第一个参数
            String first = args[1];
            // 判断是否有该指令
            if (!commandMap.containsKey(first)) {
                sender.sendMessage(manager.prefix + manager.no_command_found);
                return;
            }
            // 获取第一个参数对应的指令
            Map.Entry<Cmd, Method> info = commandMap.get(first);
            Cmd annotation = info.getKey();
            Method method = info.getValue();

            // 判断命令是否只能由玩家执行
            if (annotation.onlyPlayer() && !(sender instanceof Player)) {
                sender.sendMessage(manager.prefix + manager.command_only_player);
                return;
            }
            // 判断权限
            if (!sender.hasPermission(annotation.perm())) {
                sender.sendMessage(manager.prefix + manager.no_permission);
                return;
            }
            // 判断参数是否正确
            String availableArgs = annotation.availableArgs();
            if ((args.length - 2 == 0 && !availableArgs.contains(" ")) || args.length - 2 != availableArgs.split(" ").length) {
                sender.sendMessage(manager.prefix + manager.command_wrong
                        .replace("%first%", first)
                        .replace("%args%", availableArgs));
                return;
            }
            List<String> argList = new ArrayList<>(Arrays.asList(args));
            // 移除前两项
            argList.subList(0, 2).clear();
            try {
                // 执行指令
                if (!(boolean) method.invoke(instance, manager, sender, argList.toArray(new String[]{}))) {
                    sender.sendMessage(manager.prefix + manager.command_wrong
                            .replace("%first%", first)
                            .replace("%args%", availableArgs));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Cmd(
            name = "create",
            perm = "apex.command.create",
            availableArgs = "<游戏名>"
    )
    private boolean create(final MessageManager manager, final CommandSender sender, final String[] args) {
        String name = args[0];
        // 判断游戏存在性
        if (FileUtil.isGameExist(name)) {
            sender.sendMessage(manager.prefix + manager.game_already_exist);
            return true;
        }
        // 创建游戏
        if (FileUtil.createGame(name)) {
            sender.sendMessage(manager.prefix + manager.create_successfully);
        } else {
            sender.sendMessage(manager.prefix + manager.create_fail);
        }
        return true;
    }

    @Cmd(
            name = "setregion",
            onlyPlayer = true,
            perm = "apex.command.setregion",
            availableArgs = "<游戏名> <min/max>"
    )
    private boolean setRegion(final MessageManager manager, final CommandSender sender, final String[] args) {
        String name = args[0];
        // 判断游戏存在性
        Game game = GameUtil.getGame(name);
        if (game == null) {
            sender.sendMessage(manager.prefix + manager.game_does_not_exist);
            return true;
        }
        Player p = (Player) sender;
        AxisAlignedBB axisAlignedBB = game.getRegion() == null ? new AxisAlignedBB() : game.getRegion();
        // 设置区域
        if (args[1].equalsIgnoreCase("min")) {
            axisAlignedBB.setMin(p.getLocation().toVector());
            axisAlignedBB.setWorld(p.getWorld());
            game.setRegion(axisAlignedBB);
            sender.sendMessage(manager.prefix + manager.set_region_successfully);
        } else if (args[1].equalsIgnoreCase("max")) {
            axisAlignedBB.setMax(p.getLocation().toVector());
            axisAlignedBB.setWorld(p.getWorld());
            game.setRegion(axisAlignedBB);
            sender.sendMessage(manager.prefix + manager.set_region_successfully);
        } else {
            return false;
        }
        return true;
    }

    @Cmd(
            name = "setlobby",
            onlyPlayer = true,
            perm = "apex.command.setlobby",
            availableArgs = "<游戏名>"
    )
    private boolean setLobby(final MessageManager manager, final CommandSender sender, final String[] args) {
        String name = args[0];
        // 判断游戏存在性
        Game game = GameUtil.getGame(name);
        if (game == null) {
            sender.sendMessage(manager.prefix + manager.game_does_not_exist);
            return true;
        }
        Player p = (Player) sender;
        // 设置大厅
        game.setLobby(p.getLocation());
        sender.sendMessage(manager.prefix + manager.lobby_set_successfully);
        return true;
    }

    @Cmd(
            name = "addresource",
            onlyPlayer = true,
            perm = "apex.command.addresource",
            availableArgs = "<游戏名> <种类>"
    )
    private boolean addResource(final MessageManager manager, final CommandSender sender, final String[] args) {
        String name = args[0];
        // 判断游戏存在性
        Game game = GameUtil.getGame(name);
        if (game == null) {
            sender.sendMessage(manager.prefix + manager.game_does_not_exist);
            return true;
        }
        Player p = (Player) sender;
        // 添加资源点
        game.setResources(game.getResources() == null ? new ArrayList<>() : game.getResources());
        game.getResources().add(new Resource(args[1], p.getLocation()));
        sender.sendMessage(manager.prefix + manager.resource_add_successfully);
        return true;
    }

    @Cmd(
            name = "addteam",
            onlyPlayer = true,
            perm = "apex.command.addteam",
            availableArgs = "<游戏名> <队名> <颜色>"
    )
    private boolean addTeam(final MessageManager manager, final CommandSender sender, final String[] args) {
        String name = args[0];
        // 判断游戏存在性
        Game game = GameUtil.getGame(name);
        if (game == null) {
            sender.sendMessage(manager.prefix + manager.game_does_not_exist);
            return true;
        }
        Player p = (Player) sender;
        // 添加队伍
        game.setTeams(game.getTeams() == null ? new ArrayList<>() : game.getTeams());
        game.getTeams().add(new Team(args[1], args[2], p.getLocation()));
        sender.sendMessage(manager.prefix + manager.team_add_successfully);
        return true;
    }

    @Cmd(
            name = "save",
            perm = "apex.command.save",
            availableArgs = "<游戏名>"
    )
    private boolean save(final MessageManager manager, final CommandSender sender, final String[] args) {
        String name = args[0];
        // 判断游戏存在性
        Game game = GameUtil.getGame(name);
        if (game == null) {
            sender.sendMessage(manager.prefix + manager.game_does_not_exist);
            return true;
        }
        File gameFile = new File(ApexBattle.getInstance().getDataFolder(), "game/" + name + ".yml");
        // 保存游戏信息
        if (!game.saveToFile(gameFile)) {
            sender.sendMessage(manager.prefix + manager.save_fail);
        }
        World world = game.getRegion() == null ? null : game.getRegion().getWorld();
        if (world == null) {
            sender.sendMessage(manager.prefix + manager.save_fail);
            return true;
        }
        // 保存地图
        if (!WorldUtil.saveWorld(world, game.getName())) {
            sender.sendMessage(manager.prefix + manager.save_fail);
            return true;
        }
        sender.sendMessage(manager.prefix + manager.save_successfully);
        return true;
    }

    @Cmd(
            name = "join",
            onlyPlayer = true,
            perm = "apex.command.use",
            availableArgs = "<游戏名>"
    )
    private boolean join(final MessageManager manager, final CommandSender sender, final String[] args) {
        String name = args[0];
        Player p = (Player) sender;
        // 判断是否已经加入游戏了
        Game playing = GameUtil.getGamePlaying(p);
        if (playing != null) {
            sender.sendMessage(manager.prefix + manager.already_in_game);
            return true;
        }
        // 判断游戏存在性
        Game game = GameUtil.getGame(name);
        if (game == null) {
            sender.sendMessage(manager.prefix + manager.game_does_not_exist);
            return true;
        }
        // 加入游戏
        game.addPlayer(p);
        return true;
    }

    @Cmd(
            name = "leave",
            onlyPlayer = true,
            perm = "apex.command.use",
            availableArgs = "<游戏名>"
    )
    private boolean leave(final MessageManager manager, final CommandSender sender, final String[] args) {
        String name = args[0];
        Player p = (Player) sender;
        // 判断是否在游戏中
        Game playing = GameUtil.getGamePlaying(p);
        if (playing == null) {
            sender.sendMessage(manager.prefix + manager.not_in_game);
            return true;
        }
        // 退出游戏
        playing.removePlayer(p);
        return true;
    }
}