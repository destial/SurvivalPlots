package xyz.destiall.survivalplots.commands.sub;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.events.PlotResetEvent;
import xyz.destiall.survivalplots.events.PlotRestoreEvent;
import xyz.destiall.survivalplots.hooks.DynmapHook;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.Schematic;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class AdminRestore extends SubCommand {
    public AdminRestore() {
        super("mod.restore");
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

        if (args.length == 0) {
            sender.sendMessage(color("&cUsage: /plots restore [player-name]"));
            return;
        }

        String playerName = args[0];
        String currentOwner = plot.getRawOwner();
        if (!playerName.equalsIgnoreCase(currentOwner)) {
            sender.sendMessage(Messages.Key.PLAYER_NOT_OWNED_BEFORE.get(player, plot));
            return;
        }

        if (!new PlotRestoreEvent(plot).callEvent()) {
            SurvivalPlotsPlugin.getInst().info("PlotRestoreEvent was cancelled, skipping reset plot...");
            return;
        }

        plotPlayer.setConfirmation(() -> {
            Schematic def = WorldEditHook.loadPlot(plot, currentOwner);
            if (def != null) {
                if (plot.loadSchematic(def)) {
                    player.sendMessage(color("&aSuccessfully restored plot " + plot.getId()));
                } else {
                    player.sendMessage(color("&cUnable to restore plot " + plot.getId()));
                }
            }

            DynmapHook.updatePlot(plot);
        });

        TextComponent component = new TextComponent(color("&eType &6/plot confirm &eto confirm restoring this plot."));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(color("&aClick to confirm"))));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot confirm"));
        player.sendMessage(component);
        player.sendMessage(color("&cWARNING!! Restoring will remove everything in this current state!"));
    }
}
