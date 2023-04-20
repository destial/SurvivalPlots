package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class Desc extends SubCommand {
    public Desc() {
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
        if (plot.getOwner() != plotPlayer && (!plot.hasFlag(PlotFlags.MEMBER_EDIT_DESCRIPTION) || !plotPlayer.isMember(plot))) {
            player.sendMessage(Messages.Key.NO_PERMS_ON_PLOT.get(player, plot));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(color("&cUsage: /plot description [description]"));
            return;
        }

        String desc = ChatColor.stripColor(color(String.join(" ", args)));
        plot.setDescription(desc);

        player.sendMessage(color("&aSet your plot's description to: "));
        player.sendMessage(color("&6" + plot.getDescription()));
    }
}
