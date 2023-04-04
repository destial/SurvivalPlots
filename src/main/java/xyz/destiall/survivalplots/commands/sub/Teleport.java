package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;
import java.util.stream.Collectors;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Teleport extends SubCommand {
    public Teleport() {
        super("user.teleport");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cYou need to be a player!"));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(color("&cUsage: /plot tp [owner]"));
            return;
        }

        String name = args[0];
        for (SurvivalPlot plot : plugin.getPlotManager().getAllPlots()) {
            if (plot.getOwner() == null)
                continue;

            if (plot.getRawOwner().equalsIgnoreCase(name)) {
                try {
                    ((Player) sender).teleportAsync(plot.getHome());
                } catch (Exception e) {
                    ((Player) sender).teleport(plot.getHome());
                }
                sender.sendMessage(Messages.Key.TELEPORT_OTHERS.get((Player) sender, plot));
                return;
            }
        }
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length == 0)
            return super.tab(sender, args);

        return plugin.getPlotManager().getAllPlots().stream()
                .filter(p -> p.getOwner() != null)
                .map(SurvivalPlot::getRawOwner)
                .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }
}
