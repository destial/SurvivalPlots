package xyz.destiall.survivalplots.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyManager {
    private final SurvivalPlotsPlugin plugin;
    private final Map<String, Bank> accounts;

    private final int plotCost;
    private final int plotReset;
    private Material economyMaterial;
    private Economy vault;

    public Bank newBank(Player player) {
        if (economyMaterial == null && vault != null) {
            return new VaultBank(this, vault, player);
        }

        return new Bank(this, player);
    }

    public EconomyManager(SurvivalPlotsPlugin plugin) {
        this.plugin = plugin;
        accounts = new ConcurrentHashMap<>();

        plotCost = plugin.getConfig().getInt("plot-cost", 100);
        plotReset = plugin.getConfig().getInt("plot-reset", 5);
        String currency = plugin.getConfig().getString("plot-money", "DIAMOND");
        if (currency.equalsIgnoreCase("vault")) {
            plugin.info("Config detected vault as currency...");
            if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
                plugin.info("Vault detected!");
                RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    vault = rsp.getProvider();
                }
                plugin.warning("Vault does not have an economy registered! Defaulting back...");
            }
            plugin.warning("Vault was not detected! Defaulting back...");

        }
        economyMaterial = Material.getMaterial(currency);
        if (economyMaterial == null) {
            plugin.warning("Unable to parse plot-money: " + plugin.getConfig().getString("plot-money"))
                  .warning("Defaulting to DIAMOND...");
            economyMaterial = Material.DIAMOND;
        }
    }

    public Bank getBank(Player player) {
        if (accounts.putIfAbsent(player.getName(), newBank(player)) == null) {
            plugin.getScheduler().runTaskLater(() -> accounts.remove(player.getName()), 60L * 20L);
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

    public String getCurrency() {
        return economyMaterial == null ? "$" : economyMaterial.name();
    }

    public String formatCost(int cost) {
        if (economyMaterial == null) {
            return "$" + cost;
        }
        return cost + " " + economyMaterial.name();
    }

    public String formattedPlotCost() {
        return formatCost(plotCost);
    }

    public String formattedPlotResetCost() {
        return formatCost(plotReset);
    }
}
