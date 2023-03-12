package xyz.destiall.survivalplots.plot;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.player.PlotPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

                SurvivalPlot plot = new SurvivalPlot(i, plotSection.getConfigurationSection(id));
                plots.add(plot);
                plugin.getLogger().info("Loaded plot " + i);
            }
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

    public List<SurvivalPlot> getOwnedPlots(Player owner) {
        PlotPlayer player = plugin.getPlotPlayerManager().getPlotPlayer(owner.getName());
        List<SurvivalPlot> owned = new ArrayList<>();
        for (SurvivalPlot plot : plots) {
            if (player.isOwner(plot)) {
                owned.add(plot);
            }
        }
        return owned;
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
            plot.disableTimer();
            saveToFile();
            File plotsBackup = new File(SurvivalPlotsPlugin.getPlugin(SurvivalPlotsPlugin.class).getDataFolder(), "backups" + File.separator + plot.getId() + File.separator);
            if (plotsBackup.exists()) {
                delete(plotsBackup);
            }
        }
        return true;
    }

    public void disable() {
        for (SurvivalPlot plot : plots) {
            plot.disableTimer();
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
