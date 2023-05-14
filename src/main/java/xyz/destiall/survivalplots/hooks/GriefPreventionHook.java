package xyz.destiall.survivalplots.hooks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PistonMode;
import me.ryanhamshire.GriefPrevention.events.ClaimChangeEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimCreatedEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimExtendEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimResizeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.BoundingBox;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class GriefPreventionHook implements Listener {
    private static boolean enabled = false;
    private static GriefPrevention gp;

    private GriefPreventionHook() {}

    public static void check() {
        enabled = Bukkit.getPluginManager().isPluginEnabled("GriefPrevention");
        if (!enabled)
            return;

        SurvivalPlotsPlugin.getInst().info("Hooked into GriefPrevention");
        gp = GriefPrevention.getPlugin(GriefPrevention.class);

        Bukkit.getServer().getPluginManager().registerEvents(new GriefPreventionHook(), SurvivalPlotsPlugin.getInst());
    }

    public static boolean isPistonsEnabled(SurvivalPlot plot) {
        if (!enabled || !gp.claimsEnabledForWorld(plot.getWorld()))
            return true;

        if (gp.config_pistonMovement == PistonMode.CLAIMS_ONLY)
            return gp.dataStore.getClaimAt(plot.getCenter(), true, null) != null;

        return true;
    }

    @EventHandler
    public void onClaimCreated(ClaimCreatedEvent e) {
        if (denyClaim(e.getClaim())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClaimResize(ClaimResizeEvent e) {
        if (denyClaim(e.getTo())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClaimExtend(ClaimExtendEvent e) {
        if (denyClaim(e.getTo())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClaimChange(ClaimChangeEvent e) {
        if (denyClaim(e.getTo())) {
            e.setCancelled(true);
        }
    }

    private boolean denyClaim(Claim claim) {
        Location min = claim.getLesserBoundaryCorner();
        Location max = claim.getGreaterBoundaryCorner();
        PlotManager pm = SurvivalPlotsPlugin.getInst().getPlotManager();

        SurvivalPlot plot = pm.getPlotAt(min);
        if (plot != null) {
            return true;
        }

        plot = pm.getPlotAt(max);
        if (plot != null) {
            return true;
        }

        BoundingBox bounds = new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
        for (SurvivalPlot p : pm.getAllPlots()) {
            if (p.getBounds().overlaps(bounds)) {
                return true;
            }
        }

        return false;
    }
}
