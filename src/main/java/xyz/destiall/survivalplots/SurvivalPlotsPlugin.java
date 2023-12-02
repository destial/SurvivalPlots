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
import xyz.destiall.survivalplots.listeners.PaperListener;
import xyz.destiall.survivalplots.listeners.PlotBlocksListener;
import xyz.destiall.survivalplots.listeners.PlotInventoryListener;
import xyz.destiall.survivalplots.listeners.PlotPlayerListener;
import xyz.destiall.survivalplots.player.PlotPlayerManager;
import xyz.destiall.survivalplots.plot.PlotManager;

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

        try {
            Class.forName("com.destroystokyo.paper.utils.PaperPluginLogger");
            getServer().getPluginManager().registerEvents(new PaperListener(this), this);
        } catch (Exception ignored) {}

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
}
