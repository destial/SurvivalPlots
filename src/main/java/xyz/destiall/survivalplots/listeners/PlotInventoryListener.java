package xyz.destiall.survivalplots.listeners;

import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.DoubleChestInventory;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.player.PlotPlayerManager;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PlotInventoryListener implements Listener {
    private final SurvivalPlotsPlugin plugin;

    public PlotInventoryListener(SurvivalPlotsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player)) {
            if (!(e.getInventory() instanceof DoubleChestInventory))
                return;
        }

        Player p = (Player) e.getPlayer();
        PlotManager pm = plugin.getPlotManager();

        Location location = null;
        if (e.getInventory().getHolder() instanceof BlockInventoryHolder) {
            location = ((BlockInventoryHolder) e.getInventory().getHolder()).getBlock().getLocation();
        } else if (e.getInventory().getHolder() instanceof Container) {
            location = ((Container) e.getInventory().getHolder()).getBlock().getLocation();
        } else if (e.getInventory().getHolder() instanceof DoubleChest) {
            location = ((DoubleChest) e.getInventory().getHolder()).getLocation();
        }

        if (location == null)
            return;

        SurvivalPlot plot = pm.getPlotAt(location);
        if (plot == null)
            return;

        PlotPlayerManager ppm = plugin.getPlotPlayerManager();
        PlotPlayer player = ppm.getPlotPlayer(p.getName());

        if (!player.canOpenInventory(plot)) {
            e.setCancelled(true);
            p.sendMessage(Messages.Key.NO_OPEN_INVENTORY.get(p, plot));
        }
    }
}
