package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.economy.Bank;
import xyz.destiall.survivalplots.economy.EconomyManager;
import xyz.destiall.survivalplots.hooks.Schematic;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Reset extends SubCommand {
    public Reset() {
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
            sender.sendMessage(color("&cYou do not own this plot!"));
            return;
        }
        final EconomyManager econ = plugin.getEconomyManager();

        Runnable afterConfirmation = () -> {

            Bank account = econ.getBank((Player) sender);

            if (!account.withdraw(econ.getPlotReset())) {
                sender.sendMessage(color("&cYou do not have enough to reset this plot!"));
                sender.sendMessage(color("&cCost: " + econ.getPlotReset() + " " + econ.getEconomyMaterial().name()));
                return;
            }

            sender.sendMessage(color("&eYou spent " + econ.getPlotReset() + " " + econ.getEconomyMaterial().name() + " to reset this plot!"));

            WorldEditHook.backupPlot(plot, plot.getOwner().getName());
            sender.sendMessage(color("&aSuccessfully backed-up plot " + plot.getId()));

            Schematic def = WorldEditHook.loadPlot(plot, "default");
            if (def != null) {
                plot.loadSchematic(def);
                sender.sendMessage(color("&aSuccessfully reset plot " + plot.getId()));
            }
        };

        player.setConfirmation(afterConfirmation);
        sender.sendMessage(color("&eType &6/plot confirm &eto confirm resetting your plot."));
        sender.sendMessage(color("&eResetting costs " + econ.getPlotReset() + " " + econ.getEconomyMaterial().name()));
        sender.sendMessage(color("&cWARNING!! Resetting will reset your plot to its default state!"));
    }
}
