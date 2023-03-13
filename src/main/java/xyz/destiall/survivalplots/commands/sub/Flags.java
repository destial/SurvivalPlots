package xyz.destiall.survivalplots.commands.sub;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Flags extends SubCommand {
    public Flags() {
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
        if (plot.getOwner() != player && (!plot.hasFlag(PlotFlags.MEMBER_EDIT_FLAGS) || !player.isMember(plot))) {
            sender.sendMessage(color("&cYou do not own this plot!"));
            return;
        }

        if (args.length == 0) {
            sendFlags(sender, plot);
            return;
        }

        PlotFlags flag = PlotFlags.getFlag(args[0]);
        if (flag == null) {
            sender.sendMessage(color("&cAvailable flags: " + Arrays.stream(PlotFlags.values()).map(PlotFlags::getName).collect(Collectors.joining(", "))));
            return;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("-l")) {
            sendFlags(sender, plot);
            return;
        }

        if (plot.hasFlag(flag)) {
            plot.removeFlag(flag);
            sender.sendMessage(color("&cRemoved flag " + flag.getName() + " in this plot!"));
        } else {
            plot.addFlag(flag);
            sender.sendMessage(color("&aAdded flag " + flag.getName() + " from this plot!"));
        }
    }

    private void sendFlags(CommandSender sender, SurvivalPlot plot) {
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
        return Arrays.stream(PlotFlags.values()).map(PlotFlags::getName).collect(Collectors.toList());
    }
}
