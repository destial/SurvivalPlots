package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;
import java.util.stream.Collectors;

public class Untrust extends SubCommand {
    public Untrust() {
        super("user");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPlayer(sender))
            return;

        Player player = (Player) sender;
        PlotManager pm = plugin.getPlotManager();
        SurvivalPlot plot = pm.getPlotAt(player.getLocation());
        if (plot == null) {
            player.sendMessage(Messages.Key.NOT_STANDING_ON_PLOT.get(player, null));
            return;
        }

        PlotPlayer plotPlayer = plugin.getPlotPlayerManager().getPlotPlayer(player);
        if (plot.getOwner() != plotPlayer && (!plot.hasFlag(PlotFlags.MEMBER_TRUST_OTHER) || !plotPlayer.isMember(plot))) {
            player.sendMessage(Messages.Key.NO_PERMS_ON_PLOT.get(player, plot));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(color("&cUsage: /plot untrust [player]"));
            return;
        }

        String name = args[0];
        if (!plot.untrust(name)) {
            player.sendMessage(color("&c" + name + " is not a member of this plot!"));
            return;
        }

        player.sendMessage(color("&aUntrusted " + name + " from this plot!"));
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length == 0)
            return super.tab(sender, args);

        Location location = ((Player) sender).getLocation();

        PlotManager pm = plugin.getPlotManager();
        SurvivalPlot plot = pm.getPlotAt(location);
        if (plot == null) {
            return super.tab(sender, args);
        }

        PlotPlayer player = plugin.getPlotPlayerManager().getPlotPlayer((Player) sender);
        if (plot.getOwner() != player && (!plot.hasFlag(PlotFlags.MEMBER_TRUST_OTHER) || !player.isMember(plot))) {
            return super.tab(sender, args);
        }

        return plot.getMembers().stream()
                .filter(p -> p.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }
}
