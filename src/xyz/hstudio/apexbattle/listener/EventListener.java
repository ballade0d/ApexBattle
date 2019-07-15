package xyz.hstudio.apexbattle.listener;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.game.Game;
import xyz.hstudio.apexbattle.game.internal.ItemHandler;
import xyz.hstudio.apexbattle.util.GameUtil;
import xyz.hstudio.apexbattle.util.ItemUtil;

public class EventListener implements Listener {

    public EventListener() {
        Bukkit.getPluginManager().registerEvents(this, ApexBattle.getInst());
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        for (Game game : Game.getGames()) {
            game.onQuit(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if (e.getClickedBlock().getType().name().contains("SIGN")) {
            Sign sign = (Sign) e.getClickedBlock();
            for (Game game : Game.getGames()) {
                for (Game.SignHandler signHandler : game.getSigns()) {
                    Block block = signHandler.getLoc().getBlock();
                    if (block != null && block.getType().name().contains("SIGN")) {
                        Sign sign1 = (Sign) block.getState();
                        for (int i = 0; i < 4; i++) {
                            if (!sign.getLine(i).equals(sign1.getLine(i))) {
                                return;
                            }
                        }
                        if (game.getStatus() == Game.GameStatus.WAITING) {
                            game.onJoin(e.getPlayer());
                        }
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) {
        Player p = e.getEntity();
        Game game = GameUtil.getPlayingGame(p);
        if (game != null && game.getStatus() == Game.GameStatus.GAMING) {
            e.setKeepLevel(true);
            e.setKeepInventory(true);
            short decrease = (short) ItemHandler.protectedItem.getDurability_decrease();
            for (ItemStack itemStack : p.getInventory()) {
                if (ItemUtil.isSimilar(itemStack, ItemHandler.protectedItem.createItem())) {
                    if (itemStack.getDurability() - decrease <= 0) {
                        game.setStatus(Game.GameStatus.ENDING);
                        game.getGamePlayer(p).getTeam().win();
                    } else {
                        itemStack.setDurability((short) (itemStack.getDurability() - decrease));
                    }
                }
            }
        }
    }
}