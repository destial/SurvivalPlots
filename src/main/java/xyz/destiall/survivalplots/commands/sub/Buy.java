package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.economy.Bank;
import xyz.destiall.survivalplots.economy.EconomyManager;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Buy extends SubCommand {
    public Buy() {
        super("user");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cYou need to be a player!"));
            return;
        }

        if (!sender.hasPermission("svplots.own.unlimited")) {
            List<SurvivalPlot> current = plugin.getPlotManager().getOwnedPlots((Player) sender);
            int max = 0;
            for (PermissionAttachmentInfo perm : sender.getEffectivePermissions()) {
                if (perm.getPermission().startsWith("svplots.own.")) {
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
                sender.sendMessage(color("&cYou cannot buy any more plots!"));
                return;
            }
        }

        Location location = ((Player) sender).getLocation();

        PlotManager pm = plugin.getPlotManager();
        SurvivalPlot plot = pm.getPlotAt(location);
        if (plot == null) {
            sender.sendMessage(Messages.Key.NOT_STANDING_ON_PLOT.get((Player) sender, null));
            return;
        }

        if (plot.getOwner() != null) {
            sender.sendMessage(color("&cThis plot is not available to buy!"));
            return;
        }

        EconomyManager econ = plugin.getEconomyManager();
        Bank account = econ.getBank((Player) sender);

        if (!account.withdraw(econ.getPlotCost())) {
            sender.sendMessage(color("&cYou do not have enough to purchase this plot!"));
            sender.sendMessage(color("&cCost: " + econ.getPlotCost() + " " + econ.getEconomyMaterial().name()));
            return;
        }

        sender.sendMessage(color("&eYou spent " + econ.getPlotCost() + " " + econ.getEconomyMaterial().name() + " to buy this plot!"));

        plot.setOwner(sender.getName());
        ((Player) sender).teleport(plot.getHome());
    }
}
