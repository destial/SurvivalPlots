package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.events.PlotBanEvent;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;
import java.util.stream.Collectors;

public class Ban extends SubCommand {
    public Ban() {
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
        if (plot.getOwner() != plotPlayer && (!plot.hasFlag(PlotFlags.MEMBER_BAN_OTHER) || !plotPlayer.isMember(plot))) {
            player.sendMessage(Messages.Key.NO_PERMS_ON_PLOT.get(player, plot));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(color("&cUsage: /plot ban [player]"));
            return;
        }

        String name = args[0];
        Player toBan = plugin.getServer().getPlayer(name);
        if (toBan == null) {
            player.sendMessage(color("&cPlayer is not online!"));
            return;
        }

        if (!new PlotBanEvent(plot, plugin.getPlotPlayerManager().getPlotPlayer(toBan)).callEvent()) {
            SurvivalPlotsPlugin.getInst().info("PlotBanEvent was cancelled, skipping banning player...");
            return;
        }

        if (plot.ban(toBan)) {
            player.sendMessage(color("&eBanned " + toBan.getName() + " from this plot!"));
            return;
        }

        player.sendMessage(color("&cUnable to ban " + toBan.getName() + " from this plot!"));
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
