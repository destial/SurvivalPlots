package xyz.destiall.survivalplots.hooks;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PistonMode;
import org.bukkit.Bukkit;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class GriefPreventionHook {
    private static boolean enabled = false;
    private static GriefPrevention gp;

    public static void check() {
        enabled = Bukkit.getPluginManager().isPluginEnabled("GriefPrevention");
        if (!enabled)
            return;

        SurvivalPlotsPlugin.getInst().getLogger().info("Hooked into GriefPrevention");
        gp = GriefPrevention.getPlugin(GriefPrevention.class);
    }

    public static boolean isPistonsEnabled(SurvivalPlot plot) {
        if (!enabled || !gp.claimsEnabledForWorld(plot.getWorld()))
            return true;

        return gp.config_pistonMovement != PistonMode.CLAIMS_ONLY;
    }
}
