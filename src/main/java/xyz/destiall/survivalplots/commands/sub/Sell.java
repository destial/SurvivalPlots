package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.economy.Bank;
import xyz.destiall.survivalplots.hooks.Schematic;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Sell extends SubCommand {
    public Sell() {
        super("user");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cYou need to be a player!"));
            return;
        }

        Location location = ((Player) sender).getLocation();

        PlotManager pm = plugin.getPlotManager();
        SurvivalPlot plot = pm.getPlotAt(location);
        if (plot == null) {
            sender.sendMessage(color("&cYou are not standing on a plot!"));
            return;
        }

        PlotPlayer player = plugin.getPlotPlayerManager().getPlotPlayer((Player) sender);
        if (plot.getOwner() != player) {
            sender.sendMessage(color("&cThis plot is not available to sell!"));
            return;
        }

        Runnable afterConfirmation = () -> {
            Bank bank = plugin.getEconomyManager().getBank((Player) sender);
            bank.deposit(plugin.getEconomyManager().getPlotCost() / 2);
            sender.sendMessage(color("&aRefunded back " + (plugin.getEconomyManager().getPlotCost() / 2f) + " " + plugin.getEconomyManager().getEconomyMaterial().name()));

            WorldEditHook.backupPlot(plot, plot.getOwner().getName());
            sender.sendMessage(color("&aSuccessfully backed-up plot " + plot.getId()));

            Schematic def = WorldEditHook.loadPlot(plot, "default");
            if (def != null) {
                plot.loadSchematic(def);
                sender.sendMessage(color("&aSuccessfully reset plot " + plot.getId()));
            }

            plot.getMembers().clear();
            plot.getBanned().clear();
            plot.getFlags().clear();
            plot.getFlags().addAll(PlotFlags.def());
            plot.disableTimer();
            plot.setOwner("N/A");
        };

        player.setConfirmation(afterConfirmation);
        sender.sendMessage(color("&eType &6/plot confirm &eto confirm selling your plot"));
        sender.sendMessage(color("&cWARNING!! Selling your plot will lose your ownership status!"));
        sender.sendMessage(color("&cYou will also be refunded half the buy price!"));
    }
}
