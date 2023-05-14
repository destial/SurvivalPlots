package xyz.destiall.survivalplots.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class VaultBank extends Bank {
    private final Economy vault;
    public VaultBank(EconomyManager economyManager, Economy vault, Player player) {
        super(economyManager, player);
        this.vault = vault;
    }

    @Override
    public boolean has(int amount) {
        return vault.has(player, amount);
    }

    @Override
    public boolean withdraw(int amount) {
        return vault.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public void deposit(int amount) {
        vault.depositPlayer(player, amount);
    }

    @Override
    public int getBalance() {
        return (int) vault.getBalance(player);
    }
}
