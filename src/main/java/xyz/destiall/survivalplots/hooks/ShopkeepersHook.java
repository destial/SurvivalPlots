package xyz.destiall.survivalplots.hooks;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;

public class ShopkeepersHook {
    private static boolean enabled = false;

    public static void check() {
        enabled = Bukkit.getPluginManager().getPlugin("Shopkeepers") != null;

        if (!enabled)
            return;

        SurvivalPlotsPlugin.getInst().info("Hooked into Shopkeepers");
    }

    public static void removeEntity(Entity entity) {
        if (!enabled)
            return;

        if (!ShopkeepersAPI.getShopkeeperRegistry().isShopkeeper(entity))
            return;

        Shopkeeper shopkeeper = ShopkeepersAPI.getShopkeeperRegistry().getShopkeeperByEntity(entity);
        if (shopkeeper == null)
            return;

        shopkeeper.delete();
    }
}
