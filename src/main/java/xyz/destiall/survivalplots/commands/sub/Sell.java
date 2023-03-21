package xyz.destiall.survivalplots.commands.sub;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.economy.Bank;
import xyz.destiall.survivalplots.plot.Schematic;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Sell extends SubCommand {
    public Sell() {
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
        if (plot.getOwner() != player) {
            sender.sendMessage(color("&cThis plot is not available to sell!"));
            return;
        }

        Runnable afterConfirmation = () -> {
            WorldEditHook.backupPlot(plot, plot.getOwner().getName());
            sender.sendMessage(color("&aSuccessfully backed-up plot " + plot.getId()));

            Schematic def = WorldEditHook.loadPlot(plot, "default");
            if (def != null) {
                if (plot.loadSchematic(def)) {
                    sender.sendMessage(color("&aSuccessfully reset plot " + plot.getId()));
                    Bank bank = plugin.getEconomyManager().getBank((Player) sender);
                    int cost = plugin.getEconomyManager().getPlotCost() / 2;
                    sender.sendMessage(color("&aRefunded back " + cost + " " + plugin.getEconomyManager().getEconomyMaterial().name()));

                    bank.deposit(cost);
                } else {
                    sender.sendMessage(color("&cUnable to reset plot " + plot.getId()));
                    return;
                }
            }

            plot.getMembers().clear();
            plot.getBanned().clear();
            plot.getFlags().clear();
            plot.getFlags().addAll(PlotFlags.def());
            plot.disableExpiryTimer();
            plot.setOwner("N/A");
        };

        player.setConfirmation(afterConfirmation);
        TextComponent component = new TextComponent(color("&eType &6/plot confirm &eto confirm selling your plot."));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(color("&aClick to confirm"))));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot confirm"));
        sender.sendMessage(component);
        sender.sendMessage(color("&cWARNING!! Selling your plot will lose your ownership status!"));
        sender.sendMessage(color("&cYou will also be refunded half the buy price!"));
    }
}
