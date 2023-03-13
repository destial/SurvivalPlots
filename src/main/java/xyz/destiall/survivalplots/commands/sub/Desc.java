package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Desc extends SubCommand {
    public Desc() {
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

        PlotPlayer player = plugin.getPlotPlayerManager().getPlotPlayer((Player) sender);
        if (plot.getOwner() != player && (!plot.hasFlag(PlotFlags.MEMBER_EDIT_DESCRIPTION) || !player.isMember(plot))) {
            sender.sendMessage(Messages.Key.NO_PERMS_ON_PLOT.get((Player) sender, plot));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(color("&cYou need to include a description!"));
            return;
        }

        String desc = ChatColor.stripColor(color(String.join(" ", args)));
        plot.setDescription(desc);

        sender.sendMessage(color("&aSet your plot's description to: "));
        sender.sendMessage(color("&6" + plot.getDescription()));
    }
}
