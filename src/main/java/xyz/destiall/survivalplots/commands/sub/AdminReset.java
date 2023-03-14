package xyz.destiall.survivalplots.commands.sub;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.plot.Schematic;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class AdminReset extends SubCommand {
    public AdminReset() {
        super("mod");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cYou need to be a player!"));
            return;
        }

        PlotManager pm = plugin.getPlotManager();
        SurvivalPlot plot = pm.getPlotAt(((Player) sender).getLocation());
        PlotPlayer player = plugin.getPlotPlayerManager().getPlotPlayer((Player) sender);

        if (plot == null) {
            sender.sendMessage(Messages.Key.NOT_STANDING_ON_PLOT.get((Player) sender, null));
            return;
        }

        Runnable afterConfirmation = () -> {
            WorldEditHook.backupPlot(plot, plot.getOwner().getName());
            sender.sendMessage(color("&aSuccessfully backed-up plot " + plot.getId()));

            Schematic def = WorldEditHook.loadPlot(plot, "default");
            if (def != null) {
                plot.loadSchematic(def);
                sender.sendMessage(color("&aSuccessfully reset plot " + plot.getId()));
            }
        };

        player.setConfirmation(afterConfirmation);
        TextComponent component = new TextComponent(color("&eType &6/plot confirm &eto confirm resetting this plot."));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(color("&aClick to confirm"))));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot confirm"));
        sender.sendMessage(component);
        sender.sendMessage(color("&cWARNING!! Resetting will reset this plot to its default state!"));
    }
}
