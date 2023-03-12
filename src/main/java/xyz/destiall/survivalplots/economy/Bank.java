package xyz.destiall.survivalplots.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public class Bank {
    private final EconomyManager economyManager;
    private final Player player;

    public Bank(EconomyManager economyManager, Player player) {
        this.economyManager = economyManager;
        this.player = player;
    }

    public int getBalance() {
        int total = 0;
        if (!player.isOnline())
            return total;

        PlayerInventory inventory = player.getInventory();

        for (ItemStack item : inventory) {
            if (item == null || item.getType() != economyManager.getEconomyMaterial())
                continue;

            total += item.getAmount();
        }
        return total;
    }

    public boolean has(int amount) {
        return getBalance() >= amount;
    }

    public boolean withdraw(int amount) {
        if (!has(amount))
            return false;

        int left = amount;
        for (ItemStack item : player.getInventory()) {
            if (item == null || item.getType() != economyManager.getEconomyMaterial())
                continue;

            while (left > 0 && item.getAmount() > 0) {
                item.setAmount(item.getAmount() - 1);
                left--;
            }
        }
        return true;
    }

    public void deposit(int amount) {
        ItemStack money = new ItemStack(economyManager.getEconomyMaterial());
        money.setAmount(amount);

        Map<Integer, ItemStack> drops = player.getInventory().addItem(money);
        for (ItemStack drop : drops.values()) {
            player.getWorld().dropItem(player.getLocation(), drop);
        }
    }
}
