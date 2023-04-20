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
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.Schematic;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class AdminResell extends SubCommand {
    public AdminResell() {
        super("mod.resell");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPlayer(sender))
            return;

        Player player = (Player) sender;

        PlotManager pm = plugin.getPlotManager();
        SurvivalPlot plot = pm.getPlotAt(player.getLocation());
        PlotPlayer plotPlayer = plugin.getPlotPlayerManager().getPlotPlayer(player);

        if (plot == null) {
            player.sendMessage(Messages.Key.NOT_STANDING_ON_PLOT.get(player, null));
            return;
        }

        plotPlayer.setConfirmation(() -> {
            WorldEditHook.backupPlot(plot, plot.getOwner().getName());
            player.sendMessage(color("&aSuccessfully backed-up plot " + plot.getId()));

            Schematic def = WorldEditHook.loadPlot(plot, "default");
            if (def != null) {
                if (plot.loadSchematic(def)) {
                    player.sendMessage(color("&aSuccessfully resold plot " + plot.getId()));
                } else {
                    player.sendMessage(color("&cUnable to resell plot " + plot.getId()));
                }
            }

            plot.getMembers().clear();
            plot.getBanned().clear();
            plot.getFlags().clear();
            plot.getFlags().addAll(PlotFlags.def());
            plot.disableExpiryTimer();
            plot.setOwner("N/A");
        });

        TextComponent component = new TextComponent(color("&eType &6/plot confirm &eto confirm reselling this plot."));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(color("&aClick to confirm"))));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot confirm"));
        player.sendMessage(component);
        player.sendMessage(color("&cWARNING!! Resetting will reset this plot to its default state and remove its ownership!"));
    }
}
