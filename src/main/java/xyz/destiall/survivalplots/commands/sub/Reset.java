package xyz.destiall.survivalplots.commands.sub;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.economy.Bank;
import xyz.destiall.survivalplots.economy.EconomyManager;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.Schematic;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class Reset extends SubCommand {
    public Reset() {
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
        if (plot.getOwner() != plotPlayer) {
            player.sendMessage(Messages.Key.NO_PERMS_ON_PLOT.get(player, plot));
            return;
        }
        final EconomyManager econ = plugin.getEconomyManager();

        plotPlayer.setConfirmation(() -> {
            Bank account = econ.getBank(player);

            if (!account.withdraw(econ.getPlotReset())) {
                player.sendMessage(color("&cYou do not have enough to reset this plot!"));
                player.sendMessage(color("&cCost: " + econ.getPlotReset() + " " + econ.getEconomyMaterial().name()));
                return;
            }

            player.sendMessage(color("&eYou spent " + econ.getPlotReset() + " " + econ.getEconomyMaterial().name() + " to reset this plot!"));

            WorldEditHook.backupPlot(plot, plot.getOwner().getName());
            player.sendMessage(color("&aSuccessfully backed-up plot " + plot.getId()));

            Schematic def = WorldEditHook.loadPlot(plot, "default");
            if (def != null) {
                if (plot.loadSchematic(def)) {
                    player.sendMessage(color("&aSuccessfully reset plot " + plot.getId()));
                    try {
                        player.teleportAsync(plot.getHome());
                    } catch (Exception e) {
                        player.teleport(plot.getHome());
                    }
                } else {
                    player.sendMessage(color("&cUnable to reset plot " + plot.getId()));
                    account.deposit(econ.getPlotReset());
                }
            }
        });
        TextComponent component = new TextComponent(color("&eType &6/plot confirm &eto confirm resetting your plot."));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(color("&aClick to confirm"))));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot confirm"));
        player.sendMessage(component);
        player.sendMessage(color("&eResetting costs " + econ.getPlotReset() + " " + econ.getEconomyMaterial().name()));
        player.sendMessage(color("&cWARNING!! Resetting will reset your plot to its default state!"));
    }
}
