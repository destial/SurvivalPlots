package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Untrust extends SubCommand {
    public Untrust() {
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
        if (plot.getOwner() != player && (!plot.hasFlag(PlotFlags.MEMBER_TRUST_OTHER) || !player.isMember(plot))) {
            sender.sendMessage(color("&cYou do not own this plot!"));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(color("&cYou need to mention a player to untrust!"));
            return;
        }

        String name = args[0];
        if (!plot.untrust(name)) {
            sender.sendMessage(color("&c" + name + " is not a member of this plot!"));
            return;
        }
        
        sender.sendMessage(color("&aUntrusted " + name + " from this plot!"));
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return super.tab(sender, args);
        }
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

        return plot.getMembers();
    }
}
