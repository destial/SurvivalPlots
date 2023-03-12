package xyz.destiall.survivalplots.player;

import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlotPlayerManager {
    private final Map<String, PlotPlayer> players;
    private final SurvivalPlotsPlugin plugin;

    public PlotPlayerManager(SurvivalPlotsPlugin plugin) {
        this.players = new ConcurrentHashMap<>();
        this.plugin = plugin;
    }

    public PlotPlayer getPlotPlayer(String name) {
        if (players.putIfAbsent(name, new PlotPlayer(name)) == null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> players.remove(name), 60 * 20);
        }
        return players.get(name);
    }

    public PlotPlayer getPlotPlayer(Player player) {
        return getPlotPlayer(player.getName());
    }
}
