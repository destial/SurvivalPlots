package xyz.destiall.survivalplots;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.destiall.survivalplots.commands.PlotCommand;
import xyz.destiall.survivalplots.economy.EconomyManager;
import xyz.destiall.survivalplots.hooks.DynmapHook;
import xyz.destiall.survivalplots.hooks.GriefPreventionHook;
import xyz.destiall.survivalplots.hooks.PartiesHook;
import xyz.destiall.survivalplots.hooks.PlaceholderAPIHook;
import xyz.destiall.survivalplots.hooks.ShopkeepersHook;
import xyz.destiall.survivalplots.hooks.WorldGuardHook;
import xyz.destiall.survivalplots.listeners.PlotBlocksListener;
import xyz.destiall.survivalplots.listeners.PlotInventoryListener;
import xyz.destiall.survivalplots.listeners.PlotPlayerListener;
import xyz.destiall.survivalplots.player.PlotPlayerManager;
import xyz.destiall.survivalplots.plot.PlotManager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class SurvivalPlotsPlugin extends JavaPlugin {
    private PlotPlayerManager plotPlayerManager;
    private PlotManager plotManager;
    private EconomyManager economyManager;
    private Scheduler scheduler;

    private static SurvivalPlotsPlugin INST;

    @Override
    public void onEnable() {
        INST = this;
        this.scheduler = new Scheduler(this);
        saveDefaultConfig();
        plotPlayerManager = new PlotPlayerManager(this);
        plotManager = new PlotManager(this);
        economyManager = new EconomyManager(this);

        getServer().getPluginManager().registerEvents(new PlotBlocksListener(this), this);
        getServer().getPluginManager().registerEvents(new PlotInventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlotPlayerListener(this), this);

        getCommand("svplots").setExecutor(new PlotCommand(this));

        WorldGuardHook.check();
        DynmapHook.check();
        GriefPreventionHook.check();
        PlaceholderAPIHook.check();
        PartiesHook.check();
        ShopkeepersHook.check();

        Messages.init(this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        plotManager.update();
        plotManager.disable();
        PlaceholderAPIHook.disable();

        getCommand("svplots").setExecutor(null);
        scheduler.cancelTasks();
    }

    public void reload() {
        onDisable();
        onEnable();
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

    public Scheduler getScheduler() {
        return scheduler;
    }

    public static SurvivalPlotsPlugin getInst() {
        return INST;
    }

    public SurvivalPlotsPlugin info(String msg) {
        getLogger().info(msg);
        return this;
    }

    public SurvivalPlotsPlugin warning(String msg) {
        getLogger().warning(msg);
        return this;
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
        try {
            return Duration.of(Integer.parseInt(numberString.toString()), unit);
        } catch (Exception e) {
            getInst().getLogger().severe("Unable to parse duration " + s);
            return Duration.ZERO;
        }
    }

    public static Duration relativeDuration(Date end) {
        if (end == null)
            return Duration.ZERO;

        Date start = new Date();
        long difference_In_Time = end.getTime() - start.getTime();
        return Duration.of(difference_In_Time, ChronoUnit.MILLIS);
    }

    public static String relativeDate(Date end) {
        if (end == null)
            return "N/A";

        Date start = new Date();
        // Calculate time difference
        // in milliseconds
        long difference_In_Time
                = end.getTime() - start.getTime();

        String format = "";


        long difference_In_Days
                = (difference_In_Time
                / (1000L * 60 * 60 * 24))
                % 365;

        if (difference_In_Days > 0) {
            format += difference_In_Days + " days ";
        }

        long difference_In_Hours
                = (difference_In_Time
                / (1000L * 60 * 60))
                % 24;

        if (difference_In_Hours > 0) {
            format += difference_In_Hours + " hrs ";
        }

        long difference_In_Minutes
                = (difference_In_Time
                / (1000L * 60))
                % 60;

        if (difference_In_Minutes > 0) {
            format += difference_In_Minutes + " mins ";
        }

        long difference_In_Seconds
                = (difference_In_Time
                / 1000L)
                % 60;

        if (difference_In_Seconds > 0) {
            format += difference_In_Seconds + " s";
        }

        return format.trim();
    }
}
