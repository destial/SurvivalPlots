package xyz.destiall.survivalplots.hooks;

import org.bukkit.Bukkit;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;

public class PlaceholderAPIHook {
    private static PAPIHook hook;

    private PlaceholderAPIHook() {}

    public static void check() {
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            return;

        SurvivalPlotsPlugin.getInst().info("Hooked into PlaceholderAPI");

        hook = new PAPIHook();
        hook.register();
    }

    public static void disable() {
        if (hook == null)
            return;

        hook.unregister();
        hook = null;
    }
}
