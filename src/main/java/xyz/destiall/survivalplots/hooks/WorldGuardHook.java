package xyz.destiall.survivalplots.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class WorldGuardHook {
    private static boolean enabled = false;

    private WorldGuardHook() {}

    public static void check() {
        enabled = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard");

        SurvivalPlotsPlugin.getInst().info("Hooked into WorldGuard");
    }

    private static RegionManager getRegionManager(Location location) {
        return getRegionManager(location.getWorld());
    }

    private static RegionManager getRegionManager(World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    }

    public static boolean canPlace(Player player, Location location) {
        if (!enabled)
            return true;

        RegionManager rm = getRegionManager(location);
        if (rm == null)
            return true;

        ApplicableRegionSet rs = rm.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return rs.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD, Flags.BLOCK_PLACE);
    }

    public static boolean canBreak(Player player, Location location) {
        if (!enabled)
            return true;

        RegionManager rm = getRegionManager(location);
        if (rm == null)
            return true;
        ApplicableRegionSet rs = rm.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return rs.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD, Flags.BLOCK_BREAK);
    }

    public static boolean canUse(Player player, Location location) {
        if (!enabled)
            return true;

        RegionManager rm = getRegionManager(location);
        if (rm == null)
            return true;
        ApplicableRegionSet rs = rm.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return rs.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.USE);
    }

    public static boolean canInteract(Player player, Location location) {
        if (!enabled)
            return true;

        RegionManager rm = getRegionManager(location);
        if (rm == null)
            return true;
        ApplicableRegionSet rs = rm.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return rs.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.INTERACT);
    }

    public static boolean canPlaceVehicles(Player player, Location location) {
        if (!enabled)
            return true;

        RegionManager rm = getRegionManager(location);
        if (rm == null)
            return true;
        ApplicableRegionSet rs = rm.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return rs.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD, Flags.PLACE_VEHICLE);
    }

    public static boolean canBreakVehicles(Player player, Location location) {
        if (!enabled)
            return true;

        RegionManager rm = getRegionManager(location);
        if (rm == null)
            return true;

        ApplicableRegionSet rs = rm.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return rs.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD, Flags.DESTROY_VEHICLE);
    }

    public static boolean canBuild(Player player, Location location) {
        if (!enabled) return true;

        RegionManager rm = getRegionManager(location);
        if (rm == null)
            return true;

        ApplicableRegionSet rs = rm.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return rs.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD);
    }

    public static ProtectedRegion createRegion(SurvivalPlot plot) {
        String id = "survival-plots-" + plot.getId();
        RegionManager rm = getRegionManager(plot.getCenter());
        if (rm == null)
            return null;

        ProtectedRegion existing = rm.getRegion(id);
        if (existing != null)
            return existing;

        BlockVector3 min = BlockVector3.at(plot.getMin().getBlockX(), plot.getMin().getBlockY(), plot.getMin().getBlockZ());
        BlockVector3 max = BlockVector3.at(plot.getMax().getBlockX(), plot.getMax().getBlockY(), plot.getMax().getBlockZ());
        ProtectedCuboidRegion region = new ProtectedCuboidRegion(id, min, max);
        region.setPriority(10);
        region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);
        region.setFlag(Flags.DESTROY_VEHICLE, StateFlag.State.ALLOW);
        region.setFlag(Flags.PLACE_VEHICLE, StateFlag.State.ALLOW);

        rm.addRegion(region);
        return region;
    }

    public static void saveRegionManager(World world) {
        RegionManager rm = getRegionManager(world);
        if (rm == null)
            return;

        try {
            rm.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeRegion(SurvivalPlot plot) {
        String id = "survival-plots-" + plot.getId();
        RegionManager rm = getRegionManager(plot.getCenter());
        if (rm == null)
            return;

        rm.removeRegion(id);
    }
}
