package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Home extends SubCommand {
    public Home() {
        super("user");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cYou need to be a player!"));
            return;
        }

        List<SurvivalPlot> plots = plugin.getPlotManager().getOwnedPlots((Player) sender);
        SurvivalPlot plot = plots.get(0);
        if (args.length > 0) {
            try {
                plot = plots.get(Integer.parseInt(args[0]) - 1);
            } catch (Exception e) {
                sender.sendMessage(color("&cInvalid plot number!"));
                return;
            }
        }

        if (plot == null) {
            sender.sendMessage(color("&cYou have no plots!"));
            return;
        }

        ((Player)sender).teleport(plot.getHome());
        sender.sendMessage(Messages.Key.TELEPORT_HOME.get((Player) sender, plot));
    }
}
