package xyz.destiall.survivalplots.listeners;

import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PaperListener implements Listener {

    private final SurvivalPlotsPlugin plugin;
    public PaperListener(SurvivalPlotsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBeacon(BeaconEffectEvent e) {
        PlotManager pm = plugin.getPlotManager();
        SurvivalPlot plot = pm.getPlotAt(e.getBlock().getLocation());
        if (plot == null)
            return;

        SurvivalPlot plotAtPlayer = pm.getPlotAt(e.getPlayer().getLocation());
        if (plot != plotAtPlayer) {
            e.setCancelled(true);
        }
    }
}
