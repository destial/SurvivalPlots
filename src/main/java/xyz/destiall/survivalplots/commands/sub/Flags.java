package xyz.destiall.survivalplots.commands.sub;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Flags extends SubCommand {
    public Flags() {
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
        if (plot.getOwner() != plotPlayer && (!plot.hasFlag(PlotFlags.MEMBER_EDIT_FLAGS) || !plotPlayer.isMember(plot))) {
            player.sendMessage(Messages.Key.NO_PERMS_ON_PLOT.get(player, plot));
            return;
        }

        if (args.length == 0) {
            sendFlags(player, plot);
            return;
        }

        PlotFlags flag = PlotFlags.getFlag(args[0]);
        if (flag == null) {
            player.sendMessage(color("&cAvailable flags: " + Arrays.stream(PlotFlags.values()).map(PlotFlags::getName).collect(Collectors.joining(", "))));
            return;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("-l")) {
            if (plot.hasFlag(flag)) {
                plot.removeFlag(flag);
            } else {
                plot.addFlag(flag);
            }
            sendFlags(player, plot);
            return;
        }

        if (plot.hasFlag(flag)) {
            plot.removeFlag(flag);
            player.sendMessage(color("&cRemoved flag " + flag.getName() + " in this plot!"));
        } else {
            plot.addFlag(flag);
            player.sendMessage(color("&aAdded flag " + flag.getName() + " from this plot!"));
        }
    }

    private void sendFlags(Player sender, SurvivalPlot plot) {
        sender.sendMessage(color("&6Plot Flags:"));
        for (PlotFlags flag : PlotFlags.values()) {
            String c = plot.hasFlag(flag) ? "&a" : "&c";
            TextComponent component = new TextComponent(color(c + flag.getName() + ": " + (plot.hasFlag(flag) ? "ON" : "OFF")));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(color("&6" + flag.getDescription()))));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot flags " + flag.getName() + " -l"));
            sender.sendMessage(component);
        }
        sender.sendMessage(color("&6-=-=-=-=-=-=-=-=-=-=-"));
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return Arrays.stream(PlotFlags.values())
                .map(PlotFlags::getName)
                .filter(f -> f.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }
}
