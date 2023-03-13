package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Info extends SubCommand {
    public Info() {
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
            sender.sendMessage(Messages.Key.NOT_STANDING_ON_PLOT.get((Player) sender, null));
            return;
        }

        sender.sendMessage(Messages.Key.INFO.get((Player) sender, plot));
    }
}
