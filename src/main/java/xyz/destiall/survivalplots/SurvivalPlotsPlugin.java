package xyz.destiall.survivalplots;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import xyz.destiall.survivalplots.commands.PlotCommand;
import xyz.destiall.survivalplots.economy.EconomyManager;
import xyz.destiall.survivalplots.hooks.DynmapHook;
import xyz.destiall.survivalplots.hooks.GriefPreventionHook;
import xyz.destiall.survivalplots.hooks.PlaceholderAPIHook;
import xyz.destiall.survivalplots.hooks.WorldGuardHook;
import xyz.destiall.survivalplots.listeners.PlotBlocksListener;
import xyz.destiall.survivalplots.listeners.PlotInventoryListener;
import xyz.destiall.survivalplots.listeners.PlotPlayerListener;
import xyz.destiall.survivalplots.player.PlotPlayerManager;
import xyz.destiall.survivalplots.plot.PlotManager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.logging.Logger;

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
        DynmapHook.check();
        GriefPreventionHook.check();
        PlaceholderAPIHook.check();
        Messages.init(this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        plotManager.update();
        plotManager.disable();
        PlaceholderAPIHook.disable();

        getCommand("svplots").setExecutor(null);
        getServer().getScheduler().cancelTasks(this);
    }

    public void reload() {
        HandlerList.unregisterAll(this);

        plotManager.update();
        plotManager.disable();
        getCommand("svplots").setExecutor(null);

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
        return Duration.of(Integer.parseInt(numberString.toString()), unit);
    }

    public static String relativeDate(Date end) {

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

    public static BukkitTask runAsync(Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(getInst(), runnable);
    }

    public static BukkitTask run(Runnable runnable) {
        return Bukkit.getScheduler().runTask(getInst(), runnable);
    }

    public static BukkitTask scheduleAsync(Runnable runnable, long ticks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(getInst(), runnable, ticks);
    }

    public static BukkitTask schedule(Runnable runnable, long ticks) {
        return Bukkit.getScheduler().runTaskLater(getInst(), runnable, ticks);
    }

    public static BukkitTask repeatAsync(Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(getInst(), runnable, delay, period);
    }

    public static BukkitTask repeat(Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimer(getInst(), runnable, delay, period);
    }
}
