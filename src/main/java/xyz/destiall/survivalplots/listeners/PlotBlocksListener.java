package xyz.destiall.survivalplots.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.util.Vector;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.hooks.WorldGuardHook;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.player.PlotPlayerManager;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PlotBlocksListener implements Listener {
    private final SurvivalPlotsPlugin plugin;

    public PlotBlocksListener(SurvivalPlotsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e) {
        PlotManager pm = plugin.getPlotManager();

        SurvivalPlot plot = pm.getPlotAt(e.getBlock().getLocation());
        if (plot == null)
            return;

        PlotPlayerManager ppm = plugin.getPlotPlayerManager();
        PlotPlayer player = ppm.getPlotPlayer(e.getPlayer());
        if (!player.canBuild(plot)) {
            e.setCancelled(true);
            e.setDropItems(false);
            e.getPlayer().sendMessage(Messages.Key.NO_BUILD.get(e.getPlayer(), plot));
            return;
        }

        if (!WorldGuardHook.canBreak(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(false);
            e.setDropItems(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent e) {
        PlotManager pm = plugin.getPlotManager();

        SurvivalPlot plot = pm.getPlotAt(e.getBlock().getLocation());
        if (plot == null)
            return;

        PlotPlayerManager ppm = plugin.getPlotPlayerManager();
        PlotPlayer player = ppm.getPlotPlayer(e.getPlayer());
        if (!player.canBuild(plot)) {
            e.setCancelled(true);
            e.setBuild(false);
            e.getPlayer().sendMessage(Messages.Key.NO_BUILD.get(e.getPlayer(), plot));
            return;
        }

        if (!WorldGuardHook.canPlace(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(false);
            e.setBuild(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPowered(BlockRedstoneEvent e) {
        PlotManager pm = plugin.getPlotManager();

        SurvivalPlot plot = pm.getPlotAt(e.getBlock().getLocation());
        if (plot == null)
            return;

        if (!plot.hasFlag(PlotFlags.REDSTONE_ON)) {
            e.setNewCurrent(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDamage(BlockDamageEvent e) {
        PlotManager pm = plugin.getPlotManager();

        SurvivalPlot plot = pm.getPlotAt(e.getBlock().getLocation());
        if (plot == null)
            return;

        PlotPlayerManager ppm = plugin.getPlotPlayerManager();
        PlotPlayer player = ppm.getPlotPlayer(e.getPlayer());
        if (!player.canBuild(plot)) {
            e.setCancelled(true);
            e.setInstaBreak(false);
            e.getPlayer().sendMessage(Messages.Key.NO_BUILD.get(e.getPlayer(), plot));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent e) {
        PlotManager pm = plugin.getPlotManager();

        SurvivalPlot plot = pm.getPlotAt(e.getBlock().getLocation());
        if (plot == null) {
            e.blockList().removeIf(block -> pm.getPlotAt(block.getLocation()) != null);
            return;
        }

        if (!plot.hasFlag(PlotFlags.EXPLOSIONS_ON)) {
            e.setCancelled(true);
            e.setYield(0);
            return;
        }

        e.blockList().removeIf(block -> plot != pm.getPlotAt(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockMultiPlace(BlockMultiPlaceEvent e) {
        PlotManager pm = plugin.getPlotManager();

        SurvivalPlot plot = pm.getPlotAt(e.getBlock().getLocation());
        if (plot == null) {
            for (BlockState state : e.getReplacedBlockStates()) {
                if (pm.getPlotAt(state.getLocation()) != null) {
                    e.setCancelled(true);
                    e.setBuild(false);
                    e.getPlayer().sendMessage(Messages.Key.NO_BUILD.get(e.getPlayer(), plot));
                    return;
                }
            }
            return;
        }

        PlotPlayerManager ppm = plugin.getPlotPlayerManager();
        PlotPlayer player = ppm.getPlotPlayer(e.getPlayer());
        if (!player.canBuild(plot)) {
            e.setCancelled(true);
            e.setBuild(false);
            e.getPlayer().sendMessage(Messages.Key.NO_BUILD.get(e.getPlayer(), plot));
            return;
        }

        if (!WorldGuardHook.canPlace(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(false);
            e.setBuild(true);
        }

        for (BlockState state : e.getReplacedBlockStates()) {
            if (plot != pm.getPlotAt(state.getLocation())) {
                e.setCancelled(true);
                e.setBuild(false);
                e.getPlayer().sendMessage(Messages.Key.NO_BUILD.get(e.getPlayer(), plot));
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockForm(BlockSpreadEvent e) {
        PlotManager pm = plugin.getPlotManager();

        if (pm.getPlotAt(e.getSource().getLocation()) != pm.getPlotAt(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    /// Source code: PlotSquared v6
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        PlotManager pm = plugin.getPlotManager();

        Block block = event.getBlock();
        Location location = block.getLocation();
        BlockFace face = event.getDirection();
        Vector relative = new Vector(face.getModX(), face.getModY(), face.getModZ());
        SurvivalPlot plot = pm.getPlotAt(location);
        if (plot == null) {
            for (Block block1 : event.getBlocks()) {
                Location bloc = block1.getLocation();
                if (pm.getPlotAt(bloc) != null || pm.getPlotAt(bloc.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ())) != null) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (pm.getPlotAt(location.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ())) != null) {
                // Prevent pistons from extending if they are: bordering a plot
                // area, facing inside plot area, and not pushing any blocks
                event.setCancelled(true);
            }
            return;
        }

        for (Block block1 : event.getBlocks()) {
            Location bloc = block1.getLocation();
            Location newLoc = bloc.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ());
            if (!plot.contains(bloc) || !plot.contains(newLoc)) {
                event.setCancelled(true);
                return;
            }
            if (plot != pm.getPlotAt(bloc) || plot != pm.getPlotAt(newLoc)) {
                event.setCancelled(true);
                return;
            }
        }
        if (pm.getPlotAt(location.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ())) != null) {
            // Prevent pistons from extending if they are: bordering a plot
            // area, facing inside plot area, and not pushing any blocks
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        PlotManager pm = plugin.getPlotManager();

        Block block = event.getBlock();
        Location location = block.getLocation();
        BlockFace face = event.getDirection();
        Vector relative = new Vector(face.getModX(), face.getModY(), face.getModZ());
        SurvivalPlot plot = pm.getPlotAt(location);
        if (plot == null) {
            for (Block block1 : event.getBlocks()) {
                Location bloc = block1.getLocation();
                Location newLoc = bloc.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ());
                if (pm.getPlotAt(bloc) != null || pm.getPlotAt(newLoc) != null) {
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        for (Block block1 : event.getBlocks()) {
            Location bloc = block1.getLocation();
            Location newLoc = bloc.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ());
            if (!plot.contains(bloc) || !plot.contains(newLoc)) {
                event.setCancelled(true);
                return;
            }
            if (plot != pm.getPlotAt(bloc) || plot != pm.getPlotAt(newLoc)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
