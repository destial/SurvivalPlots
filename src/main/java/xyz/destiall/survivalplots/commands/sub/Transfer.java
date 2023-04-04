package xyz.destiall.survivalplots.commands.sub;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;
import java.util.stream.Collectors;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Transfer extends SubCommand {
    public Transfer() {
        super("user.transfer");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cYou need to be a player!"));
            return;
        }

        Player p = (Player) sender;

        SurvivalPlot plot = plugin.getPlotManager().getPlotAt(p.getLocation());
        if (plot == null) {
            sender.sendMessage(Messages.Key.NOT_STANDING_ON_PLOT.get(p, null));
            return;
        }

        PlotPlayer player = plugin.getPlotPlayerManager().getPlotPlayer(p);
        if (plot.getOwner() != player) {
            sender.sendMessage(Messages.Key.NO_PERMS_ON_PLOT.get(p, plot));
            return;
        }

        if (args.length == 0) {
            p.sendMessage(color("&cUsage: /plot transfer [player]"));
            return;
        }

        String name = args[0];
        PlotPlayer newOwner = plugin.getPlotPlayerManager().getPlotPlayer(name);
        if (!newOwner.isOnline()) {
            sender.sendMessage(color("&cPlayer is not online!"));
            return;
        }

        if (plot.getOwner() == newOwner) {
            sender.sendMessage(color("&cYou cannot transfer this plot to yourself!"));
            return;
        }

        player.setConfirmation(() -> {
            WorldEditHook.backupPlot(plot, plot.getRawOwner());
            sender.sendMessage(color("&aSuccessfully backed-up plot " + plot.getId()));

            plot.setOwner(newOwner.getName());
            sender.sendMessage(color("&aYou have transferred this plot's ownership to " + newOwner.getName()));
        });

        TextComponent component = new TextComponent(color("&eType &6/plot confirm &eto confirm transferring your plot."));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(color("&aClick to confirm"))));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot confirm"));
        sender.sendMessage(component);
        sender.sendMessage(color("&cWARNING!! Once transferred, you will not have access to the plot anymore!"));
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
