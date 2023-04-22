package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.economy.Bank;
import xyz.destiall.survivalplots.economy.EconomyManager;
import xyz.destiall.survivalplots.events.PlotBuyEvent;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;

public class Buy extends SubCommand {
    public Buy() {
        super("user");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPlayer(sender))
            return;

        Player player = (Player) sender;

        if (!player.hasPermission("svplots.own.unlimited")) {
            List<SurvivalPlot> current = plugin.getPlotManager().getOwnedPlots(player);
            int max = 0;
            for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
                if (perm.getPermission().startsWith("svplots.own.") && perm.getValue()) {
                    try {
                        int amount = Integer.parseInt(perm.getPermission().substring(perm.getPermission().lastIndexOf(".") + 1));
                        if (amount > max) {
                            max = amount;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (current.size() >= max) {
                player.sendMessage(color("&cYou cannot buy any more plots!"));
                return;
            }
        }

        PlotManager pm = plugin.getPlotManager();
        SurvivalPlot plot = pm.getPlotAt(player.getLocation());
        if (plot == null) {
            player.sendMessage(Messages.Key.NOT_STANDING_ON_PLOT.get(player, null));
            return;
        }

        if (plot.getOwner() != null) {
            player.sendMessage(color("&cThis plot is not available to buy!"));
            return;
        }

        EconomyManager econ = plugin.getEconomyManager();
        Bank account = econ.getBank(player);

        if (!account.has(econ.getPlotCost())) {
            player.sendMessage(color("&cYou do not have enough to purchase this plot!"));
            player.sendMessage(color("&cCost: " + econ.getPlotCost() + " " + econ.getEconomyMaterial().name()));
            return;
        }

        if (!new PlotBuyEvent(plot).callEvent()) {
            SurvivalPlotsPlugin.getInst().info("PlotBuyEvent was cancelled, skipping buying plot...");
            return;
        }

        account.withdraw(econ.getPlotCost());

        player.sendMessage(color("&eYou spent " + econ.getPlotCost() + " " + econ.getEconomyMaterial().name() + " to buy this plot!"));

        plot.setOwner(player.getName());
        try {
            player.teleportAsync(plot.getHome());
        } catch (Exception e) {
            player.teleport(plot.getHome());
        }
    }
}
