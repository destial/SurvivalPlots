package xyz.destiall.survivalplots.hooks;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;

public class PartiesHook {
    private static boolean enabled = false;
    private static PartiesAPI api;

    public static void check() {
        enabled = Bukkit.getServer().getPluginManager().isPluginEnabled("Parties");
        if (!enabled) return;

        SurvivalPlotsPlugin.getInst().info("Hooked into Parties");
        api = Parties.getApi();
    }

    public static boolean sameParty(OfflinePlayer one, OfflinePlayer two) {
        if (!enabled) return false;

        Party partyOne = api.getParty(one.getUniqueId());
        return partyOne != null && partyOne == api.getParty(two.getUniqueId());
    }
}
