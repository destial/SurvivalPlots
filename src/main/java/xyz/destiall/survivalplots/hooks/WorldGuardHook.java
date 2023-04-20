package xyz.destiall.survivalplots.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;

public class WorldGuardHook {
    private static boolean enabled = false;
    private WorldGuardHook() {}

    public static void check() {
        enabled = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard");

        SurvivalPlotsPlugin.getInst().info("Hooked into WorldGuard");
    }

    private static RegionManager getRegionManager(Location location) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
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
}
