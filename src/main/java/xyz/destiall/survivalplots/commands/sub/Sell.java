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
import xyz.destiall.survivalplots.economy.Bank;
import xyz.destiall.survivalplots.events.PlotSellEvent;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.Schematic;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class Sell extends SubCommand {
    public Sell() {
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
            player.sendMessage(color("&cThis plot is not available to sell!"));
            return;
        }

        if (!new PlotSellEvent(plot).callEvent()) {
            SurvivalPlotsPlugin.getInst().info("PlotSellEvent was cancelled, skipping selling plot...");
            return;
        }

        plotPlayer.setConfirmation(() -> {
            WorldEditHook.backupPlot(plot, plot.getOwner().getName());
            player.sendMessage(color("&aSuccessfully backed-up plot " + plot.getId()));

            Schematic def = WorldEditHook.loadPlot(plot, "default");
            if (def != null) {
                if (plot.loadSchematic(def)) {
                    player.sendMessage(color("&aSuccessfully reset plot " + plot.getId()));
                    Bank bank = plugin.getEconomyManager().getBank(player);
                    int cost = plugin.getEconomyManager().getPlotCost() / 2;
                    player.sendMessage(color("&aRefunded back " + cost + " " + plugin.getEconomyManager().getEconomyMaterial().name()));
                    bank.deposit(cost);
                    try {
                        player.teleportAsync(plot.getHome());
                    } catch (Exception e) {
                        player.teleport(plot.getHome());
                    }
                } else {
                    player.sendMessage(color("&cUnable to reset plot " + plot.getId()));
                    return;
                }
            }

            plot.getMembers().clear();
            plot.getBanned().clear();
            plot.getFlags().clear();
            plot.getFlags().addAll(PlotFlags.def());
            plot.disableExpiryTimer();
            plot.setOwner("N/A");
        });

        TextComponent component = new TextComponent(color("&eType &6/plot confirm &eto confirm selling your plot."));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(color("&aClick to confirm"))));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot confirm"));
        player.sendMessage(component);
        player.sendMessage(color("&cWARNING!! Selling your plot will lose your ownership status!"));
        player.sendMessage(color("&cYou will also be refunded half the buy price!"));
    }
}
