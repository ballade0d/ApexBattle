package xyz.hstudio.apexbattle.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.game.Game;
import xyz.hstudio.apexbattle.util.GameUtil;

public class EventListener implements Listener {

    public EventListener() {
        Bukkit.getPluginManager().registerEvents(this, ApexBattle.getInstance());
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
    public void onEntityToggleGlide(final EntityToggleGlideEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        Player p = (Player) e.getEntity();
        Game game = GameUtil.getGamePlaying(p);
        if (game == null) {
            return;
        }
        if (game.getStatue() == Game.GameStatue.GAMING) {
            p.setVelocity(p.getVelocity().add(new Vector(0, ApexBattle.getInstance().getConfigManager().special_elytra_y_addition, 0)));
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
            e.setKeepInventory(true);
            e.setKeepLevel(true);
            game.onDeath(p);
        }
    }
}