package xyz.destiall.survivalplots.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.plot.Schematic;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldEditHook {
    private WorldEditHook() {}

    public static BoundingBox getSelection(Player player) {
        try {
            WorldEditPlugin pl = WorldEditPlugin.getPlugin(WorldEditPlugin.class);
            Region selection = pl.getSession(player).getSelection();
            return BoundingBox.of(adapt(selection.getMinimumPoint()), adapt(selection.getMaximumPoint()));
        } catch (Exception ignored) {}
        return null;
    }

    public static Vector adapt(BlockVector3 vector3) {
        return new Vector(vector3.getX(), vector3.getY(), vector3.getZ());
    }

    public static BlockVector3 adapt(Vector vector) {
        return BlockVector3.at(vector.getX(), vector.getY(), vector.getZ());
    }

    public static CuboidRegion adapt(World world, BoundingBox bounds) {
        return new CuboidRegion(BukkitAdapter.adapt(world), adapt(bounds.getMin()), adapt(bounds.getMax()));
    }

    public static CuboidRegion getRegion(SurvivalPlot plot) {
        return adapt(plot.getWorld(), plot.getBounds());
    }

    public static void backupPlot(SurvivalPlot plot, String name) {
        File plotsBackup = new File(SurvivalPlotsPlugin.getInst().getDataFolder(), "backups" + File.separator + plot.getId() + File.separator);
        if (!plotsBackup.exists()) {
            try {
                plotsBackup.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        CuboidRegion region = getRegion(plot);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        Schematic schematic = new Schematic(plot, clipboard);
        SurvivalPlotsPlugin.getInst().info("Backing up plot " + plot.getId() + ": " + name + "...");
        schematic.getCompoundTag(plot).whenComplete((tag, err) -> {
            if (err != null) {
                err.printStackTrace();
                return;
            }
            try {
                SurvivalPlotsPlugin.getInst().info("Backed-up plot " + plot.getId() + ": " + name);
                schematic.save(tag, new File(plotsBackup, name + ".schem"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static List<String> getBackupsList(SurvivalPlot plot) {
        File plotsBackup = new File(SurvivalPlotsPlugin.getInst().getDataFolder(), "backups" + File.separator + plot.getId() + File.separator);
        if (!plotsBackup.exists()) {
            try {
                plotsBackup.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File[] files = plotsBackup.listFiles();
        if (files == null)
            return Collections.emptyList();

        return Stream.of(files).map(f -> f.getName().replace(".schem", "")).collect(Collectors.toList());
    }

    public static Schematic loadPlot(SurvivalPlot plot, String name) {
        File plotsBackup = new File(SurvivalPlotsPlugin.getInst().getDataFolder(), "backups" + File.separator + plot.getId() + File.separator);
        if (!plotsBackup.exists())
            return null;

        try {
            return new Schematic(new File(plotsBackup, name + ".schem"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
