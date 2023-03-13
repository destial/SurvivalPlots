package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;
import java.util.stream.Collectors;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class AdminDelete extends SubCommand {
    public AdminDelete() {
        super("admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cYou need to be a player!"));
            return;
        }

        if (args.length == 0) {
            PlotManager pm = plugin.getPlotManager();
            SurvivalPlot plot = pm.getPlotAt(((Player) sender).getLocation());
            if (plot == null) {
                sender.sendMessage(color("&cYou are not standing on a plot!"));
                return;
            }

            if (plugin.getPlotManager().deletePlot(plot)) {
                sender.sendMessage(color("&aDeleted plot id " + plot.getId()));
            }
            return;
        }

        int id = Integer.parseInt(args[0]);
        if (plugin.getPlotManager().deletePlot(id)) {
            sender.sendMessage(color("&aDeleted plot id " + id));
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
