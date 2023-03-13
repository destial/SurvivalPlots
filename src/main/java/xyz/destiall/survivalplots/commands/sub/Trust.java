package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;
import java.util.stream.Collectors;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Trust extends SubCommand {
    public Trust() {
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
            sender.sendMessage(color("&cYou need to mention a player to trust!"));
            return;
        }

        String name = args[0];
        Player toTrust = plugin.getServer().getPlayer(name);
        if (toTrust == null) {
            sender.sendMessage(color("&cPlayer is not online!"));
            return;
        }

        if (plot.trust(toTrust)) {
            sender.sendMessage(color("&aTrusted " + toTrust.getName() + " in this plot!"));
            return;
        }

        sender.sendMessage(color("&cAlready trusted " + toTrust.getName() + " in this plot!"));
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        if (args.length == 0)
            return super.tab(sender, args);

        Player player = (Player) sender;
        return Bukkit.getOnlinePlayers().stream().filter(player::canSee).map(Player::getName).filter(p -> p.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
    }
}
