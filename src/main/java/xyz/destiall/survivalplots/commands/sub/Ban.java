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

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Ban extends SubCommand {
    public Ban() {
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
        if (plot.getOwner() != player && (!plot.hasFlag(PlotFlags.MEMBER_BAN_OTHER) || !player.isMember(plot))) {
            sender.sendMessage(Messages.Key.NO_PERMS_ON_PLOT.get((Player) sender, plot));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(color("&cUsage: /plot ban [player]"));
            return;
        }

        String name = args[0];
        Player toBan = plugin.getServer().getPlayer(name);
        if (toBan == null) {
            sender.sendMessage(color("&cPlayer is not online!"));
            return;
        }

        if (plot.ban(toBan)) {
            sender.sendMessage(color("&eBanned " + toBan.getName() + " from this plot!"));
            return;
        }

        sender.sendMessage(color("&cUnable to ban " + toBan.getName() + " from this plot!"));
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        if (args.length == 0)
            return super.tab(sender, args);

        Player player = (Player) sender;
        return plugin.getServer().getOnlinePlayers().stream()
                .filter(player::canSee).map(Player::getName)
                .filter(p -> p.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }
}
