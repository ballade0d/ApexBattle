package xyz.hstudio.apexbattle;

import org.bukkit.DyeColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import xyz.hstudio.apexbattle.game.Game;
import xyz.hstudio.apexbattle.game.internal.ItemHandler;
import xyz.hstudio.apexbattle.util.GameUtil;
import xyz.hstudio.apexbattle.util.NumberUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class ApexCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6§lApexBattle §7§l>> §r§eApexBattle 1.0 by MrCraftGoo");
            return true;
        }
        if (sender.isOp()) {
            switch (args[0].toLowerCase()) {
                case "create": {
                    if (args.length == 5) {
                        File file = new File(ApexBattle.getInst().getDataFolder(), "game/" + args[1] + ".yml");
                        if (file.exists()) {
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e游戏已存在！");
                            return true;
                        }
                        if (!NumberUtil.isInt(args[2]) || !NumberUtil.isInt(args[3]) || !NumberUtil.isInt(args[4])) {
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e参数错误！");
                            return true;
                        }
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e创建游戏失败！");
                            return true;
                        }
                        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                        config.set("name", args[1]);
                        config.set("team_size", Integer.parseInt(args[2]));
                        config.set("min_player", Integer.parseInt(args[3]));
                        config.set("max_player", Integer.parseInt(args[4]));
                        try {
                            config.save(file);
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e创建成功！");
                        } catch (IOException e) {
                            e.printStackTrace();
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e创建游戏失败！");
                            return true;
                        }
                    } else {
                        sender.sendMessage("§6§lApexBattle §7§l>> §r§e指令错误！");
                        return true;
                    }
                    break;
                }
                case "setlobby": {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (args.length == 2) {
                            File file = new File(ApexBattle.getInst().getDataFolder(), "game/" + args[1] + ".yml");
                            if (!file.exists()) {
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e游戏不存在！");
                                return true;
                            }
                            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                            String world = p.getWorld().getName();
                            double x = p.getLocation().getX();
                            double y = p.getLocation().getY();
                            double z = p.getLocation().getZ();
                            float yaw = p.getLocation().getYaw();
                            float pitch = p.getLocation().getPitch();
                            config.set("lobby.world", world);
                            config.set("lobby.x", x);
                            config.set("lobby.y", y);
                            config.set("lobby.z", z);
                            config.set("lobby.yaw", yaw);
                            config.set("lobby.pitch", pitch);
                            try {
                                config.save(file);
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e设置成功！");
                            } catch (IOException e) {
                                e.printStackTrace();
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e设置失败！");
                                return true;
                            }
                        } else {
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e指令错误！");
                            return true;
                        }
                    } else {
                        sender.sendMessage("§6§lApexBattle §7§l>> §r§e该指令只能由玩家执行！");
                        return true;
                    }
                    break;
                }
                case "addteam": {
                    if (args.length == 4) {
                        File file = new File(ApexBattle.getInst().getDataFolder(), "game/" + args[1] + ".yml");
                        if (!file.exists()) {
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e游戏不存在！");
                            return true;
                        }
                        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                        if (config.contains("teams." + args[3])) {
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e队伍已存在！");
                            return true;
                        }
                        if (Arrays.stream(DyeColor.values()).noneMatch(v -> v.name().equals(args[2]))) {
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e颜色错误！");
                            return true;
                        }
                        config.set("teams." + args[3] + ".color", args[2]);
                        try {
                            config.save(file);
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e队伍创建成功！");
                        } catch (IOException e) {
                            e.printStackTrace();
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e设置失败！");
                            return true;
                        }
                    } else {
                        sender.sendMessage("§6§lApexBattle §7§l>> §r§e指令错误！");
                        return true;
                    }
                    break;
                }
                case "setspawn": {
                    if (sender instanceof Player) {
                        if (args.length == 3) {
                            File file = new File(ApexBattle.getInst().getDataFolder(), "game/" + args[1] + ".yml");
                            if (!file.exists()) {
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e游戏不存在！");
                                return true;
                            }
                            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                            if (!config.contains("teams." + args[2])) {
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e队伍不存在！");
                                return true;
                            }
                            Player p = (Player) sender;
                            String world = p.getWorld().getName();
                            double x = p.getLocation().getX();
                            double y = p.getLocation().getY();
                            double z = p.getLocation().getZ();
                            float yaw = p.getLocation().getYaw();
                            float pitch = p.getLocation().getPitch();
                            config.set("teams." + args[2] + ".world", world);
                            config.set("teams." + args[2] + ".x", x);
                            config.set("teams." + args[2] + ".y", y);
                            config.set("teams." + args[2] + ".z", z);
                            config.set("teams." + args[2] + ".yaw", yaw);
                            config.set("teams." + args[2] + ".pitch", pitch);
                            try {
                                config.save(file);
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e队伍设置成功！");
                            } catch (IOException e) {
                                e.printStackTrace();
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e设置失败！");
                                return true;
                            }
                        } else {
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e指令错误！");
                            return true;
                        }
                    } else {
                        sender.sendMessage("§6§lApexBattle §7§l>> §r§e该指令只能由玩家执行！");
                        return true;
                    }
                    break;
                }
                case "addresource": {
                    if (sender instanceof Player) {
                        if (args.length == 3) {
                            File file = new File(ApexBattle.getInst().getDataFolder(), "game/" + args[1] + ".yml");
                            if (!file.exists()) {
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e游戏不存在！");
                                return true;
                            }
                            if (ItemHandler.resource.stream().noneMatch(res -> res.getId().equalsIgnoreCase(args[2]))) {
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e该资源类型不存在！");
                                return true;
                            }
                            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                            int max = -1;
                            if (config.isSet("resource")) {
                                Set<String> sections = config.getConfigurationSection("resource").getKeys(false);
                                for (String section : sections) {
                                    if (NumberUtil.isInt(section) && Integer.parseInt(section) > max) {
                                        max = Integer.parseInt(section);
                                    }
                                }
                                max++;
                            } else {
                                max = 1;
                            }
                            Player p = (Player) sender;
                            String world = p.getWorld().getName();
                            double x = p.getLocation().getX();
                            double y = p.getLocation().getY();
                            double z = p.getLocation().getZ();
                            float yaw = p.getLocation().getYaw();
                            float pitch = p.getLocation().getPitch();
                            config.set("resource." + max + ".type", args[2]);
                            config.set("resource." + max + ".world", world);
                            config.set("resource." + max + ".x", x);
                            config.set("resource." + max + ".y", y);
                            config.set("resource." + max + ".z", z);
                            config.set("resource." + max + ".yaw", yaw);
                            config.set("resource." + max + ".pitch", pitch);
                            try {
                                config.save(file);
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e资源点添加成功！");
                            } catch (IOException e) {
                                e.printStackTrace();
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e设置失败！");
                                return true;
                            }
                        } else {
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e指令错误！");
                            return true;
                        }
                    } else {
                        sender.sendMessage("§6§lApexBattle §7§l>> §r§e该指令只能由玩家执行！");
                        return true;
                    }
                    break;
                }
                case "setregion": {
                    if (sender instanceof Player) {
                        if (args.length == 3) {
                            File file = new File(ApexBattle.getInst().getDataFolder(), "game/" + args[1] + ".yml");
                            if (!file.exists()) {
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e游戏不存在！");
                                return true;
                            }
                            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                            Player p = (Player) sender;
                            if (args[2].equalsIgnoreCase("min")) {
                                if (config.contains("region.world")) {
                                    if (!p.getWorld().getName().equalsIgnoreCase(config.getString("region.world"))) {
                                        sender.sendMessage("§6§lApexBattle §7§l>> §r§e游戏区域不在一个世界！");
                                        return true;
                                    }
                                }
                                config.set("region.world", p.getWorld().getName());
                                config.set("region.x1", p.getLocation().getX());
                                config.set("region.y1", p.getLocation().getY());
                                config.set("region.z1", p.getLocation().getZ());
                                try {
                                    config.save(file);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    sender.sendMessage("§6§lApexBattle §7§l>> §r§e设置失败！");
                                    return true;
                                }
                            } else if (args[2].equalsIgnoreCase("max")) {
                                if (config.contains("region.world")) {
                                    if (!p.getWorld().getName().equalsIgnoreCase(config.getString("region.world"))) {
                                        sender.sendMessage("§6§lApexBattle §7§l>> §r§e游戏区域不在一个世界！");
                                        return true;
                                    }
                                }
                                config.set("region.world", p.getWorld().getName());
                                config.set("region.x2", p.getLocation().getX());
                                config.set("region.y2", p.getLocation().getY());
                                config.set("region.z2", p.getLocation().getZ());
                                try {
                                    config.save(file);
                                    sender.sendMessage("§6§lApexBattle §7§l>> §r§e设置成功！");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    sender.sendMessage("§6§lApexBattle §7§l>> §r§e设置失败！");
                                    return true;
                                }
                            } else {
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e指令错误！");
                                return true;
                            }
                        }
                    } else {
                        sender.sendMessage("§6§lApexBattle §7§l>> §r§e该指令只能由玩家执行！");
                        return true;
                    }
                    break;
                }
                default: {
                    sender.sendMessage("§6§lApexBattle §7§l>> §r§e指令错误！");
                    return true;
                }
            }
        } else {
            switch (args[0]) {
                case "join": {
                    if (sender instanceof Player) {
                        if (args.length == 2) {
                            Player p = (Player) sender;
                            Game game = GameUtil.getPlayingGame(p);
                            if (game != null) {
                                sender.sendMessage("§6§lApexBattle §7§l>> §r§e你已经在游戏中了！");
                                return true;
                            }
                            for (Game join : Game.getGames()) {
                                if (join.getName().equals(args[1])) {
                                    if (join.getStatus() == Game.GameStatus.WAITING) {
                                        join.onJoin(p);
                                    } else {
                                        sender.sendMessage("§6§lApexBattle §7§l>> §r§e现在无法加入该游戏！");
                                        return true;
                                    }
                                    break;
                                }
                            }
                        } else {
                            sender.sendMessage("§6§lApexBattle §7§l>> §r§e指令错误！");
                            return true;
                        }
                    } else {
                        sender.sendMessage("§6§lApexBattle §7§l>> §r§e该指令只能由玩家执行！");
                        return true;
                    }
                    break;
                }
            }
        }
        return true;
    }
}