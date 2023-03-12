package xyz.destiall.survivalplots.economy;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyManager {
    private final SurvivalPlotsPlugin plugin;
    private final Map<String, Bank> accounts;

    private final int plotCost;
    private final int plotReset;
    private Material economyMaterial;

    public EconomyManager(SurvivalPlotsPlugin plugin) {
        this.plugin = plugin;
        accounts = new ConcurrentHashMap<>();

        plotCost = plugin.getConfig().getInt("plot-cost", 100);
        plotReset = plugin.getConfig().getInt("plot-reset", 5);
        economyMaterial = Material.getMaterial(plugin.getConfig().getString("plot-money", "DIAMOND"));
        if (economyMaterial == null) {
            plugin.getLogger().info("Unable to parse plot-money: " + plugin.getConfig().getString("plot-money"));
            plugin.getLogger().info("Defaulting to DIAMOND...");
            economyMaterial = Material.DIAMOND;
        }
    }

    public Bank getBank(Player player) {
        if (accounts.putIfAbsent(player.getName(), new Bank(this, player)) == null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> accounts.remove(player.getName()), 60 * 20);
        }
        return accounts.get(player.getName());
    }

    public int getPlotCost() {
        return plotCost;
    }

    public int getPlotReset() {
        return plotReset;
    }

    public Material getEconomyMaterial() {
        return economyMaterial;
    }
}
