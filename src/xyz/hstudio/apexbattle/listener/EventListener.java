package xyz.hstudio.apexbattle.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.util.Vector;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.game.Game;
import xyz.hstudio.apexbattle.packet.ChannelHandler;
import xyz.hstudio.apexbattle.util.GameUtil;

public class EventListener implements Listener {

    public EventListener() {
        Bukkit.getPluginManager().registerEvents(this, ApexBattle.getInstance());
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
        ChannelHandler.register(e.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if (!e.hasBlock() || !e.hasItem()) {
            return;
        }
        Player p = e.getPlayer();
        Game game = GameUtil.getGamePlaying(p);
        if (game == null) {
            return;
        }
        if (game.getStatue() == Game.GameStatue.GAMING) {
            ItemStack itemStack = e.getItem();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (e.getItem().getType() != Material.EGG) {
                return;
            }
            SpawnEggMeta meta = (SpawnEggMeta) itemMeta;
            if (meta.getSpawnedType() != EntityType.GUARDIAN) {
                return;
            }
            // TODO: Implement
            Guardian guardian = p.getWorld().spawn(e.getClickedBlock().getLocation().clone().add(0, 1, 0), Guardian.class);
            guardian.setAI(false);
            guardian.setCollidable(false);

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
            // TODO: Customizable
            projectile.setVelocity(projectile.getVelocity().multiply(1.3));
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
            // TODO: Customizable
            p.setVelocity(p.getVelocity().add(new Vector(0, 5, 0)));
        }
    }
}