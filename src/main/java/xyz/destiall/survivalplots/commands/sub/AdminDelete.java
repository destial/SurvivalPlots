package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.events.PlotDeleteEvent;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;
import java.util.stream.Collectors;

public class AdminDelete extends SubCommand {
    public AdminDelete() {
        super("admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPlayer(sender))
            return;

        Player player = (Player) sender;

        if (args.length == 0) {
            PlotManager pm = plugin.getPlotManager();
            SurvivalPlot plot = pm.getPlotAt(player.getLocation());
            if (plot == null) {
                player.sendMessage(Messages.Key.NOT_STANDING_ON_PLOT.get(player, null));
                return;
            }

            if (plugin.getPlotManager().deletePlot(plot)) {
                player.sendMessage(color("&aDeleted plot id " + plot.getId()));
            }
            return;
        }

        int id = Integer.parseInt(args[0]);
        if (plugin.getPlotManager().deletePlot(id)) {
            player.sendMessage(color("&aDeleted plot id " + id));
        }
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        if (args.length == 0)
            return super.tab(sender, args);

        return plugin.getPlotManager().getAllPlots().stream()
                .map(p -> ""+p.getId())
                .filter(id -> id.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }
}
