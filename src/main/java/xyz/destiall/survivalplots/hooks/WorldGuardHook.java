package xyz.destiall.survivalplots.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook {
    private static boolean enabled = false;
    private WorldGuardHook() {}

    public static void check() {
        enabled = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard");
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

        return rm.getApplicableRegions(BukkitAdapter.asBlockVector(location)).queryState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BLOCK_PLACE) == StateFlag.State.ALLOW || canBuild(player, location);
    }

    public static boolean canBreak(Player player, Location location) {
        if (!enabled)
            return true;

        RegionManager rm = getRegionManager(location);
        if (rm == null)
            return true;

        return rm.getApplicableRegions(BukkitAdapter.asBlockVector(location)).queryState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BLOCK_BREAK) == StateFlag.State.ALLOW || canBuild(player, location);
    }

    public static boolean canBuild(Player player, Location location) {
        if (!enabled) return true;

        RegionManager rm = getRegionManager(location);
        if (rm == null)
            return true;

        return rm.getApplicableRegions(BukkitAdapter.asBlockVector(location)).queryState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD) == StateFlag.State.ALLOW;
    }
}
