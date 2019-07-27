package xyz.hstudio.apexbattle.listener;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.config.ConfigManager;
import xyz.hstudio.apexbattle.config.MessageManager;
import xyz.hstudio.apexbattle.game.Game;
import xyz.hstudio.apexbattle.game.Resource;
import xyz.hstudio.apexbattle.game.Sign;
import xyz.hstudio.apexbattle.game.Team;
import xyz.hstudio.apexbattle.util.GameUtil;
import xyz.hstudio.apexbattle.util.NmsUtil;

import java.util.ArrayList;
import java.util.Map;

public class EventListener implements Listener {

    public EventListener() {
        Bukkit.getPluginManager().registerEvents(this, ApexBattle.getInstance());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(final PlayerQuitEvent e) {
        Player p = e.getPlayer();
        Game game = GameUtil.getGamePlaying(p);
        if (game != null) {
            game.removePlayer(p);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) {
            return;
        }
        Player damager = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();
        Game game = GameUtil.getGamePlaying(damager);
        if (game == null || GameUtil.getGamePlaying(victim) != game) {
            return;
        }
        if (game.getGamePlayer(damager).getTeam() == game.getGamePlayer(victim).getTeam()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFurnaceSmelt(final FurnaceSmeltEvent e) {
        ItemStack result = e.getResult();
        ItemMeta itemMeta = result.getItemMeta();
        ConfigManager manager = ApexBattle.getInstance().getConfigManager();
        for (Map.Entry<String, Map.Entry<Integer, ItemStack>> entry : manager.resourceMap.entrySet()) {
            if (entry.getValue().getValue().getType() != result.getType()) {
                continue;
            }
            itemMeta.setDisplayName(entry.getValue().getValue().getItemMeta().getDisplayName());
        }
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL || e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            return;
        }
        Player p = (Player) e.getEntity();
        Game game = GameUtil.getGamePlaying(p);
        if (game == null) {
            return;
        }
        if (game.isGodMode()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLaunch(final ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof EnderPearl) || !(e.getEntity().getShooter() instanceof Player)) {
            return;
        }
        Projectile projectile = e.getEntity();
        Player shooter = (Player) projectile.getShooter();
        Game game = GameUtil.getGamePlaying(shooter);
        if (game == null) {
            return;
        }
        if (game.getStatue() == Game.GameStatue.GAMING) {
            projectile.addPassenger(shooter);
            projectile.setVelocity(projectile.getVelocity().multiply(ApexBattle.getInstance().getConfigManager().special_ender_pearl_vec_multiplier));
        }
    }

    @EventHandler
    public void onSignChange(final SignChangeEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPermission("apex.createsign")) {
            return;
        }
        // 是游戏的牌子
        if (!e.getLine(0).equals("[ApexBattle]")) {
            return;
        }
        String name = e.getLine(1);
        Game game = GameUtil.getGame(name);
        MessageManager manager = ApexBattle.getInstance().getMessageManager();
        if (game == null) {
            // 游戏不存在
            p.sendMessage(manager.prefix + manager.create_sign_failed);
        } else {
            Block block = e.getBlock();
            Sign sign = new Sign(block.getWorld(), block.getX(), block.getY(), block.getZ());
            game.setSigns(game.getSigns() == null ? new ArrayList<>() : game.getSigns());
            game.getSigns().add(sign);
            p.sendMessage(manager.prefix + manager.create_sign_successfully);
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        // 点击的方块是牌子
        if (block == null || !block.getType().name().contains("SIGN")) {
            return;
        }
        Player p = e.getPlayer();
        ConfigManager config = ApexBattle.getInstance().getConfigManager();
        MessageManager message = ApexBattle.getInstance().getMessageManager();
        org.bukkit.block.Sign sign = (org.bukkit.block.Sign) e.getClickedBlock().getState();
        // 是游戏牌子
        if (!sign.getLine(0).equals(config.sign_first)) {
            return;
        }
        for (Game game : Game.getGames()) {
            if (game.getSigns() == null) {
                continue;
            }
            for (Sign s : game.getSigns()) {
                if (s.getWorld().equals(block.getWorld()) && s.getX() == block.getX() && s.getY() == block.getY() && s.getZ() == block.getZ()) {
                    if (game.getStatue() == Game.GameStatue.WAITING || game.getLostPlayer().containsKey(p.getUniqueId())) {
                        // 等待中，可以加入
                        game.addPlayer(p);
                    } else {
                        // 暂时无法加入
                        p.sendMessage(message.prefix + message.cannot_join);
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) {
        Player p = e.getEntity();
        Game game = GameUtil.getGamePlaying(p);
        if (game == null) {
            return;
        }
        if (game.getStatue() == Game.GameStatue.GAMING) {
            // 死亡不掉落
            e.setKeepInventory(true);
            e.setKeepLevel(true);
            game.onDeath(p);
        }

        Player killer = p.getKiller();
        if (killer == null) {
            return;
        }
        Game killerGame = GameUtil.getGamePlaying(killer);
        if (killerGame == null || game != killerGame) {
            return;
        }
        Game.GamePlayer gamePlayer = game.getGamePlayer(killer);
        if (gamePlayer == null) {
            return;
        }
        // 记录杀敌数
        gamePlayer.increaseKillCount();
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        Game game = GameUtil.getGamePlaying(p);
        if (game == null) {
            return;
        }
        Game.GamePlayer gamePlayer = game.getGamePlayer(p);
        if (gamePlayer == null) {
            return;
        }
        if (game.getStatue() == Game.GameStatue.GAMING) {
            if (gamePlayer.getTeam().isLose()) {
                e.setRespawnLocation(game.getSpectate());
            } else {
                e.setRespawnLocation(gamePlayer.getTeam().getLocation());
            }
        }
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Game game = GameUtil.getGamePlaying(p);
        if (game == null) {
            return;
        }
        if (game.getStatue() == Game.GameStatue.GAMING) {
            Block block = e.getBlock();
            for (Resource resource : game.getResources()) {
                if (resource.getLocation().getWorld().equals(block.getWorld()) && resource.getLocation().distance(block.getLocation()) <= 5) {
                    e.setCancelled(true);
                    return;
                }
            }
            for (Team team : game.getTeams()) {
                if (team.getLocation().getWorld().equals(block.getWorld()) && team.getLocation().distance(block.getLocation()) <= 5) {
                    e.setCancelled(true);
                    return;
                }
            }
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        Player p = e.getPlayer();
        Game game = GameUtil.getGamePlaying(p);
        if (game == null) {
            return;
        }
        if (game.getStatue() == Game.GameStatue.GAMING) {
            Block block = e.getBlock();
            for (Resource resource : game.getResources()) {
                if (resource.getLocation().getWorld().equals(block.getWorld()) && resource.getLocation().distance(block.getLocation()) <= 5) {
                    e.setCancelled(true);
                    return;
                }
            }
            for (Team team : game.getTeams()) {
                if (team.getLocation().getWorld().equals(block.getWorld()) && team.getLocation().distance(block.getLocation()) <= 5) {
                    e.setCancelled(true);
                    return;
                }
            }
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        Game game = GameUtil.getGamePlaying(p);
        if (game == null) {
            return;
        }
        ItemStack itemStack = e.getItemDrop().getItemStack();
        if ((NmsUtil.getInstance().hasUniqueTag(itemStack, "Protected") || NmsUtil.getInstance().hasUniqueTag(itemStack, "Shop")) && game.getStatue() == Game.GameStatue.GAMING) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        Player p = (Player) e.getEntity();
        Game game = GameUtil.getGamePlaying(p);
        if (game == null) {
            return;
        }
        if (game.getStatue() != Game.GameStatue.GAMING) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        Game game = GameUtil.getGamePlaying(p);
        if (game == null) {
            return;
        }
        String message = e.getMessage();
        boolean shout = false;
        Team team = game.getTeams().stream().filter(t -> t.getGamePlayers().stream().anyMatch(g -> g.getUuid().equals(p.getUniqueId()))).findFirst().orElse(null);
        if (message.startsWith("!")) {
            shout = true;
        }
        e.getRecipients().clear();
        if (game.getStatue() == Game.GameStatue.GAMING) {
            if (shout) {
                e.setFormat("§f[全局] [" + team.getName() + "§f] [%s§f] §7> §f%s");
                game.getGamePlayers().forEach(gamePlayer -> e.getRecipients().add(gamePlayer.getPlayer()));
                e.setMessage(message.substring(1));
            } else {
                e.setFormat("[" + team.getName() + "§f] §f[%s§f] §7> §f%s");
                team.getGamePlayers().forEach(gamePlayer -> e.getRecipients().add(gamePlayer.getPlayer()));
            }
        } else {
            if (team == null) {
                e.setFormat("§f[%s§f] §7> §f%s");
            } else {
                e.setFormat("[" + team.getName() + "] §f[%s§f] §7> §f%s");
            }
            game.getGamePlayers().forEach(gamePlayer -> e.getRecipients().add(gamePlayer.getPlayer()));
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getCurrentItem() == null) {
            return;
        }
        ItemStack itemStack = e.getCurrentItem();
        if (NmsUtil.getInstance().hasUniqueTag(itemStack, "Protected")) {
            e.setCancelled(true);
        }
    }
}