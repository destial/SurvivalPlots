package xyz.destiall.survivalplots.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.BlockInventoryHolder;
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
        if (!(e.getInventory().getHolder() instanceof BlockInventoryHolder))
            return;

        PlotManager pm = plugin.getPlotManager();
        Block block = ((BlockInventoryHolder) e.getInventory().getHolder()).getBlock();

        SurvivalPlot plot = pm.getPlotAt(block.getLocation());
        if (plot == null)
            return;

        PlotPlayerManager ppm = plugin.getPlotPlayerManager();
        PlotPlayer player = ppm.getPlotPlayer(e.getPlayer().getName());

        if (!player.canOpenInventory(plot)) {
            e.setCancelled(true);
        }
    }
}
