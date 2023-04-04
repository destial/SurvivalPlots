package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Fly extends SubCommand {
    public Fly() {
        super("user.fly");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cYou need to be a player!"));
            return;
        }

        SurvivalPlot plot = plugin.getPlotManager().getPlotAt(((Player) sender).getLocation());
        if (plot == null) {
            sender.sendMessage(Messages.Key.NOT_STANDING_ON_PLOT.get((Player) sender, null));
            return;
        }

        PlotPlayer player = plugin.getPlotPlayerManager().getPlotPlayer((Player) sender);
        if (plot.getOwner() != player && (!plot.hasFlag(PlotFlags.MEMBER_FLY) || !player.isMember(plot))) {
            sender.sendMessage(Messages.Key.NO_PERMS_ON_PLOT.get((Player) sender, plot));
            return;
        }

        Player p = (Player) sender;
        if (p.getAllowFlight()) {
            p.setFlying(false);
            p.setAllowFlight(false);
        } else {
            p.setAllowFlight(true);
            p.setFlying(true);
        }

        sender.sendMessage(color(((Player) sender).isFlying() ? "&aYou can now fly in this plot!" : "&aYou have stopped flying in this plot!"));
    }
}
