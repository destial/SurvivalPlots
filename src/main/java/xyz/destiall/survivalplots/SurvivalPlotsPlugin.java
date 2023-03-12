package xyz.destiall.survivalplots;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.destiall.survivalplots.commands.PlotCommand;
import xyz.destiall.survivalplots.economy.EconomyManager;
import xyz.destiall.survivalplots.hooks.WorldGuardHook;
import xyz.destiall.survivalplots.listeners.PlotBlocksListener;
import xyz.destiall.survivalplots.listeners.PlotInventoryListener;
import xyz.destiall.survivalplots.listeners.PlotPlayerListener;
import xyz.destiall.survivalplots.player.PlotPlayerManager;
import xyz.destiall.survivalplots.plot.PlotManager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public final class SurvivalPlotsPlugin extends JavaPlugin {
    private PlotPlayerManager plotPlayerManager;
    private PlotManager plotManager;
    private EconomyManager economyManager;

    private static SurvivalPlotsPlugin INST;

    @Override
    public void onEnable() {
        INST = this;
        saveDefaultConfig();
        plotPlayerManager = new PlotPlayerManager(this);
        plotManager = new PlotManager(this);
        economyManager = new EconomyManager(this);

        getServer().getPluginManager().registerEvents(new PlotBlocksListener(this), this);
        getServer().getPluginManager().registerEvents(new PlotInventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlotPlayerListener(this), this);

        getCommand("svplots").setExecutor(new PlotCommand(this));
        WorldGuardHook.check();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        plotManager.update();
        plotManager.disable();

        getCommand("svplots").setExecutor(null);
        getServer().getScheduler().cancelTasks(this);
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PlotPlayerManager getPlotPlayerManager() {
        return plotPlayerManager;
    }

    public static SurvivalPlotsPlugin getInst() {
        return INST;
    }

    public static Duration getDuration(String s) {
        ChronoUnit unit =
                s.endsWith("d") ? ChronoUnit.DAYS :
                s.endsWith("h") ? ChronoUnit.HOURS :
                s.endsWith("s") ? ChronoUnit.SECONDS :
                s.endsWith("m") ? ChronoUnit.MINUTES :
                s.endsWith("M") ? ChronoUnit.MONTHS :
                        ChronoUnit.MILLIS;

        StringBuilder numberString = new StringBuilder();
        int i = 0;
        while (true) {
            try {
                int n = Integer.parseInt(""+s.charAt(i++));
                numberString.append(n);
            } catch (Exception e) {
                break;
            }
        }
        return Duration.of(Integer.parseInt(numberString.toString()), unit);
    }
}
