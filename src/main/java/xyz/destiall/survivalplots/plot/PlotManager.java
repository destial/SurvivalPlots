package xyz.destiall.survivalplots.plot;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PlotManager {
    private final List<SurvivalPlot> plots;
    private final SurvivalPlotsPlugin plugin;
    private final AtomicInteger ids;
    private final File plotsFile;
    private final YamlConfiguration plotsConfig;

    public PlotManager(SurvivalPlotsPlugin plugin) {
        this.plugin = plugin;
        this.plots = new ArrayList<>();

        plotsFile = new File(plugin.getDataFolder(), "plots.yml");
        if (!plotsFile.exists()) {
            try {
                plotsFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int latestId = 0;

        plotsConfig = YamlConfiguration.loadConfiguration(plotsFile);
        ConfigurationSection plotSection = plotsConfig.getConfigurationSection("plots");
        if (plotSection != null) {
            for (String id : plotSection.getKeys(false)) {
                int i = Integer.parseInt(id);
                if (i > latestId) {
                    latestId = i;
                }

                SurvivalPlot plot = new SurvivalPlot(i, Objects.requireNonNull(plotSection.getConfigurationSection(id)));
                plots.add(plot);
            }

            plugin.info("Loaded plots [" + plots.stream().map(p -> ""+p.getId()).collect(Collectors.joining(", ")) + "] (" + plots.size() + " in size)");
        }

        plots.sort(Comparator.comparingInt(SurvivalPlot::getId));

        ids = new AtomicInteger(latestId);
    }

    public SurvivalPlot getPlotAt(Location location) {
        Vector position = location.toVector();
        for (SurvivalPlot plot : plots) {
            if (location.getWorld() == plot.getWorld() && plot.contains(position)) {
                return plot;
            }
        }
        return null;
    }

    public List<SurvivalPlot> getOwnedPlots(String name) {
        List<SurvivalPlot> owned = new ArrayList<>();
        for (SurvivalPlot plot : plots) {
            if (plot.getRawOwner().equalsIgnoreCase(name)) {
                owned.add(plot);
            }
        }
        return owned;
    }

    public List<SurvivalPlot> getOwnedPlots(Player owner) {
        return getOwnedPlots(owner.getName());
    }

    public SurvivalPlot createPlot(World world, BoundingBox bounds) {
        int id = ids.incrementAndGet();
        Location corner1 = new Location(world, bounds.getMinX(), world.getMinHeight(), bounds.getMinZ());
        Location corner2 = new Location(world, bounds.getMaxX(), world.getMaxHeight(), bounds.getMaxZ());
        BoundingBox box = BoundingBox.of(corner1, corner2);

        SurvivalPlot plot = new SurvivalPlot(id, box, world.getName());

        plot.addToConfig(plotsConfig);
        plots.add(plot);
        saveToFile();

        return plot;
    }

    public boolean deletePlot(int id) {
        SurvivalPlot plot = plots.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
        if (plot == null) {
            return false;
        }
        return deletePlot(plot);
    }

    public boolean deletePlot(SurvivalPlot plot) {
        if (plots.remove(plot)) {
            plotsConfig.set("plots." + plot.getId(), null);
            plot.disableExpiryTimer();
            saveToFile();
            File plotsBackup = new File(SurvivalPlotsPlugin.getPlugin(SurvivalPlotsPlugin.class).getDataFolder(), "backups" + File.separator + plot.getId() + File.separator);
            if (plotsBackup.exists()) {
                if (plugin.getConfig().getBoolean("async-file-operations")) {
                    SurvivalPlotsPlugin.runAsync(() -> delete(plotsBackup));
                } else {
                    delete(plotsBackup);
                }
            }
        }
        return true;
    }

    public void disable() {
        for (SurvivalPlot plot : plots) {
            plot.disableExpiryTimer();
        }
    }

    public void update() {
        updateConfig();
        saveToFile();
    }

    public void updateConfig() {
        for (SurvivalPlot plot : plots) {
            plot.addToConfig(plotsConfig);
        }
    }

    public List<SurvivalPlot> getAllPlots() {
        return plots;
    }

    public void saveToFile() {
        if (plugin.getConfig().getBoolean("async-file-operations")) {
            SurvivalPlotsPlugin.runAsync(() -> {
                try {
                    plotsConfig.save(plotsFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return;
        }
        try {
            plotsConfig.save(plotsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        f.delete();
    }
}
