package xyz.destiall.survivalplots.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
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
    public void onBlockInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null)
            return;

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            PlotManager pm = plugin.getPlotManager();

            SurvivalPlot plot = pm.getPlotAt(block.getLocation());
            if (plot == null)
                return;

            PlotPlayerManager ppm = plugin.getPlotPlayerManager();
            PlotPlayer player = ppm.getPlotPlayer(e.getPlayer());

            ItemStack item = e.getItem();
            if (item == null) {
                if (!player.canInteractBlock(plot)) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(Messages.Key.NO_INTERACT.get(e.getPlayer(), plot));
                    return;
                }
                return;
            }

            if (!player.canBuild(plot)) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Messages.Key.NO_BUILD.get(e.getPlayer(), plot));
                return;
            }

            if (!WorldGuardHook.canPlace(e.getPlayer(), block.getLocation()) || !WorldGuardHook.canBreak(e.getPlayer(), block.getLocation())) {
                e.setCancelled(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCauldronEmpty(CauldronLevelChangeEvent event) {
        Entity entity = event.getEntity();
        PlotManager pm = plugin.getPlotManager();
        SurvivalPlot plot = pm.getPlotAt(event.getBlock().getLocation());
        if (plot == null)
            return;

        // TODO Add flags for specific control over cauldron changes (rain, dripstone...)
        switch (event.getReason()) {
            case BANNER_WASH:
            case ARMOR_WASH:
            case EXTINGUISH: {
                if (entity instanceof Player) {
                    PlotPlayer player = plugin.getPlotPlayerManager().getPlotPlayer((Player) entity);
                    if (!player.canBuild(plot)) {
                        event.setCancelled(true);
                        entity.sendMessage(Messages.Key.NO_BUILD.get((Player) entity, plot));
                        return;
                    }
                }
                if (event.getReason() == CauldronLevelChangeEvent.ChangeReason.EXTINGUISH && event.getEntity() != null) {
                    event.getEntity().setFireTicks(0);
                }
                // Though the players fire ticks are modified,
                // the cauldron water level change is cancelled and the event should represent that.
                event.setCancelled(true);
                break;
            }
            default: {
                // Bucket empty, Bucket fill, Bottle empty, Bottle fill are already handled in PlayerInteract event
                // Evaporation or Unknown reasons do not need to be cancelled as they are considered natural causes
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        Vehicle entity = event.getVehicle();

        PlotManager pm = plugin.getPlotManager();
        SurvivalPlot plot = pm.getPlotAt(entity.getLocation());
        if (plot == null)
            return;

        if (event.getEntered() instanceof Player) {
            PlotPlayerManager ppm = plugin.getPlotPlayerManager();
            PlotPlayer player = ppm.getPlotPlayer((Player) event.getEntered());
            if (!player.canInteractEntity(plot)) {
                event.setCancelled(true);
                event.getEntered().sendMessage(Messages.Key.NO_INTERACT.get((Player) event.getEntered(), plot));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleMove(VehicleMoveEvent event) {
        Vehicle entity = event.getVehicle();

        PlotManager pm = plugin.getPlotManager();
        SurvivalPlot toPlot = pm.getPlotAt(event.getTo());
        SurvivalPlot fromPlot = pm.getPlotAt(event.getFrom());
        if (toPlot != fromPlot) {
            entity.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChange(BlockFromToEvent event) {
        Block fromBlock = event.getBlock();
        final Location fromLocation = fromBlock.getLocation();
        SurvivalPlot fromPlot = plugin.getPlotManager().getPlotAt(fromLocation);

        Block toBlock = event.getToBlock();
        Location toLocation = toBlock.getLocation();
        SurvivalPlot toPlot = plugin.getPlotManager().getPlotAt(toLocation);

        if (fromPlot != toPlot) {
            event.setCancelled(true);
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPowered(BlockRedstoneEvent e) {
        PlotManager pm = plugin.getPlotManager();

        SurvivalPlot plot = pm.getPlotAt(e.getBlock().getLocation());
        if (plot == null)
            return;

        if (!plot.hasFlag(PlotFlags.REDSTONE_ON)) {
            e.setNewCurrent(0);
        } else {
            e.setNewCurrent(e.getOldCurrent());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
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
            return;
        }

        if (!WorldGuardHook.canBreak(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(false);
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

    @EventHandler(priority = EventPriority.MONITOR)
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
