package xyz.destiall.survivalplots.hooks;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynmapHook {
    private static boolean enabled = false;
    private static DynmapAPI api = null;
    private static MarkerSet plotMarker = null;
    private static AreaStyle areaStyle = null;
    private static int updatePeriod = 10;
    private static Duration expiryDate;

    private static String infoWindow = "<div class=\"infowindow\"><span style=\"font-size:120%;\">Owner: %owner%</span><br /> Members: <span style=\"font-weight:bold;\">%members%</span><br /> Description: <span style=\"font-weight:bold;\">%description%</span><br /> Expiry: <span style=\"font-weight:bold;\">%expiry%</span></div>";

    private DynmapHook() {}

    public static void check() {
        Plugin plug = Bukkit.getServer().getPluginManager().getPlugin("dynmap");

        if (plug == null)
            return;

        enabled = true;
        SurvivalPlotsPlugin.getInst().info("Hooked into dynmap");

        api = (DynmapAPI) plug;
        register();
    }

    private static void register() {
        if (!enabled)
            return;

        File configFile = new File(SurvivalPlotsPlugin.getInst().getDataFolder(), "dynmap.yml");
        YamlConfiguration config = new YamlConfiguration();
        if (!configFile.exists()) {
            config.set("label", "Plots");
            config.set("hide-by-default", false);
            config.set("update-period", updatePeriod);
            config.set("info-window", infoWindow);
            config.set("expiry-date", "10d");
            config.set("area-style.stroke-color", "00FF00");
            config.set("area-style.owned-stroke-color", "FF00FF");
            config.set("area-style.expiry-stroke-color", "FF0000");
            config.set("area-style.stroke-opacity", 0.8);
            config.set("area-style.stroke-weight", 3);
            config.set("area-style.fill-color", "00FF00");
            config.set("area-style.owned-fill-color", "FF00FF");
            config.set("area-style.expiry-fill-color", "FF0000");
            config.set("area-style.fill-opacity", 0.35);
            try {
                config.save(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                config.load(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MarkerAPI markerAPI = api.getMarkerAPI();
        plotMarker = markerAPI.getMarkerSet("survival.plots");
        if (plotMarker == null) {
            plotMarker = markerAPI.createMarkerSet("survival.plots", config.getString("label", "Plots"), null, false);
        } else {
            plotMarker.setMarkerSetLabel(config.getString("label", "Plots"));
        }
        infoWindow = config.getString("info-window", infoWindow);
        plotMarker.setHideByDefault(config.getBoolean("hide-by-default"));
        updatePeriod = config.getInt("update-period", updatePeriod);
        areaStyle = new AreaStyle(config, "area-style");
        expiryDate = SurvivalPlotsPlugin.getDuration(config.getString("expiry-date", "10d"));

        startUpdater();
    }

    private static void startUpdater() {
        if (!enabled)
            return;

        final Map<Integer, AreaMarker> areaMarkers = new HashMap<>();
        final SurvivalPlotsPlugin plugin = SurvivalPlotsPlugin.getInst();

        plugin.getScheduler().runTaskTimerAsync(() -> {
            List<Integer> notUpdated = new ArrayList<>(areaMarkers.keySet());
            for (SurvivalPlot plot : SurvivalPlotsPlugin.getInst().getPlotManager().getAllPlots()) {
                AreaMarker areaMarker = areaMarkers.get(plot.getId());
                double[] x = new double[] {plot.getMin().getBlockX(), plot.getMax().getBlockX() + 1};
                double[] z = new double[] {plot.getMin().getBlockZ(), plot.getMax().getBlockZ() + 1};
                if (areaMarker == null) {
                    areaMarker = plotMarker.findAreaMarker("plot.area." + plot.getId());
                    areaMarkers.put(plot.getId(), areaMarker);
                }
                if (areaMarker == null) {
                    areaMarker = plotMarker.createAreaMarker("plot.area." + plot.getId(), String.valueOf(plot.getId()), false, plot.getWorld().getName(), x, z, false);
                    areaMarkers.put(plot.getId(), areaMarker);
                }

                updatePlot(plot, areaMarker);
                render(plot);
                notUpdated.remove((Object) plot.getId());
            }

            for (int removed : notUpdated) {
                areaMarkers.remove(removed).deleteMarker();
            }
        }, 20L, updatePeriod * 20L);
    }

    public static void updatePlot(SurvivalPlot plot) {
        updatePlot(plot, null);
    }

    public static void updatePlot(SurvivalPlot plot, AreaMarker areaMarker) {
        if (!enabled)
            return;

        if (areaMarker == null) {
            areaMarker = plotMarker.findAreaMarker("plot.area." + plot.getId());
            if (areaMarker == null)
                return;
        }

        double[] x = new double[] {plot.getMin().getBlockX(), plot.getMax().getBlockX() + 1};
        double[] z = new double[] {plot.getMin().getBlockZ(), plot.getMax().getBlockZ() + 1};
        areaMarker.setCornerLocations(x, z);
        areaMarker.setLineStyle(areaStyle.getStrokeWeight(), areaStyle.getStrokeOpacity(), areaStyle.getStrokeColor(plot));
        areaMarker.setFillStyle(areaStyle.getFillOpacity(), areaStyle.getFillColor(plot));
        areaMarker.setDescription(formatInfoWindow(plot));

        render(plot);
    }

    public static void render(SurvivalPlot plot) {
        if (!enabled)
            return;

        api.triggerRenderOfVolume(plot.getMin(), plot.getMax());
    }

    private static String formatInfoWindow(SurvivalPlot plot) {
        String v = "<div class=\"regioninfo\">" + infoWindow + "</div>";
        v = v.replace("%owner%", plot.getRawOwner());
        v = v.replace("%id%", String.valueOf(plot.getId()));
        v = v.replace("%description%", plot.getDescription());
        v = v.replace("%members%", plot.getMembers().size() > 0 ? String.join(", ", plot.getMembers()) : "None");
        v = v.replace("%banned%", plot.getBanned().size() > 0 ? String.join(", ", plot.getBanned()) : "None");
        v = v.replace("%flags%", plot.getFlags().size() > 0 ? plot.getFlags().stream().map(PlotFlags::getName).collect(Collectors.joining(", ")) : "None");
        v = v.replace("%expiry%", plot.getExpiryDate() != null ? SurvivalPlotsPlugin.relativeDate(plot.getExpiryDate()) : "N/A");
        return v;
    }

    private static class AreaStyle {
        private int strokecolor;
        private int fillcolor;

        private final double strokeopacity;
        private final int strokeweight;
        private final double fillopacity;

        private int ownedfillcolor;
        private int ownedstrokecolor;

        private int expiryfillcolor;
        private int expirystrokecolor;

        AreaStyle(FileConfiguration cfg, String path) {
            String sc = cfg.getString(path + ".stroke-color", null);
            strokeopacity = cfg.getDouble(path + ".stroke-opacity", -1);
            strokeweight = cfg.getInt(path + ".stroke-weight", -1);
            String fc = cfg.getString(path + ".fill-color", null);
            String ofc = cfg.getString(path + ".owned-fill-color", null);
            String osc = cfg.getString(path + ".owned-stroke-color", null);
            String efc = cfg.getString(path + ".expiry-fill-color", null);
            String esc = cfg.getString(path + ".expiry-stroke-color", null);

            strokecolor = -1;
            fillcolor = -1;
            ownedfillcolor = -1;
            ownedstrokecolor = -1;
            expiryfillcolor = -1;
            expirystrokecolor = -1;
            try {
                if (sc != null)
                    strokecolor = Integer.parseInt(sc, 16);
                if (fc != null)
                    fillcolor = Integer.parseInt(fc, 16);
                if (osc != null)
                    ownedstrokecolor = Integer.parseInt(osc, 16);
                if (ofc != null)
                    ownedfillcolor = Integer.parseInt(ofc, 16);
                if (efc != null)
                    expiryfillcolor = Integer.parseInt(efc, 16);
                if (esc != null)
                    expirystrokecolor = Integer.parseInt(esc, 16);
            } catch (NumberFormatException ignored) {}

            fillopacity = cfg.getDouble(path+".fill-opacity", -1);
        }

        public int getStrokeColor(SurvivalPlot plot) {
            if (plot.getOwner() == null) {
                return (strokecolor >= 0 ? strokecolor : 0xFF0000);
            }

            Duration relative = SurvivalPlotsPlugin.relativeDuration(plot.getExpiryDate());
            if (relative.minus(expiryDate).isNegative()) {
                return (expirystrokecolor >= 0 ? expirystrokecolor : 0xFF0000);
            }
            return (ownedstrokecolor >= 0 ? ownedstrokecolor : 0xFF0000);
        }

        public double getStrokeOpacity() {
            return strokeopacity >= 0 ? strokeopacity : 0.8;
        }

        public int getStrokeWeight() {
            return strokeweight >= 0 ? strokeweight : 3;
        }

        public int getFillColor(SurvivalPlot plot) {
            if (plot.getOwner() == null) {
                return (fillcolor >= 0 ? fillcolor : 0xFF0000);
            }

            Duration relative = SurvivalPlotsPlugin.relativeDuration(plot.getExpiryDate());
            if (relative.minus(expiryDate).isNegative()) {
                return (expiryfillcolor >= 0 ? expiryfillcolor : 0xFF0000);
            }
            return (ownedfillcolor >= 0 ? ownedfillcolor : 0xFF0000);
        }

        public double getFillOpacity() {
            return fillopacity >= 0 ? fillopacity : 0.35;
        }
    }
}
