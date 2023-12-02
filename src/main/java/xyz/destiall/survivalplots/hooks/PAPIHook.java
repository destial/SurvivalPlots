package xyz.destiall.survivalplots.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.destiall.survivalplots.PlotUtils;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;

class PAPIHook extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "svplots";
    }

    @Override
    public @NotNull String getAuthor() {
        return "destiall";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        PlotManager pm = SurvivalPlotsPlugin.getInst().getPlotManager();
        if (params.equalsIgnoreCase("plots")) {
            List<SurvivalPlot> owned = pm.getOwnedPlots(player.getName());
            return "" + owned.size();
        }

        SurvivalPlot plot = null;
        if (player.isOnline() && params.toLowerCase().startsWith("currentplot_")) {
            plot = pm.getPlotAt(((Player) player).getLocation());
        } else if (params.contains("_")) {
            int number;
            try {
                number = Integer.parseInt(params.split("_")[0]);
            } catch (Exception ignored) {
                return "null";
            }

            plot = pm.getAllPlots().stream().filter(p -> p.getId() == number).findFirst().orElse(null);
        }

        if (plot == null)
            return "null";

        switch (params.toLowerCase().substring(params.indexOf("_") + 1)) {
            case "owner":
                return plot.getRawOwner();
            case "description":
                return plot.getDescription();
            case "id":
                return "" + plot.getId();
            case "members":
                return plot.getMembers().size() != 0 ? String.join(", ", plot.getMembers()) : "N/A";
            case "banned":
                return plot.getBanned().size() != 0 ? String.join(", ", plot.getBanned()) : "N/A";
            case "expiry":
                return plot.getExpiryDate() != null ? PlotUtils.relativeDate(plot.getExpiryDate()) : "N/A";
        }

        return "null";
    }
}
