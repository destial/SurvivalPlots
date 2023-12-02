package xyz.destiall.survivalplots.commands.sub;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.events.PlotTransferEvent;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;
import java.util.stream.Collectors;

public class Transfer extends SubCommand {
    public Transfer() {
        super("user.transfer");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPlayer(sender))
            return;

        Player player = (Player) sender;
        SurvivalPlot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        if (plot == null) {
            player.sendMessage(Messages.Key.NOT_STANDING_ON_PLOT.get(player, null));
            return;
        }

        PlotPlayer plotPlayer = plugin.getPlotPlayerManager().getPlotPlayer(player);
        if (plot.getOwner() != plotPlayer) {
            player.sendMessage(Messages.Key.NO_PERMS_ON_PLOT.get(player, plot));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(color("&cUsage: /plot transfer [player]"));
            return;
        }

        String name = args[0];
        PlotPlayer newOwner = plugin.getPlotPlayerManager().getPlotPlayer(name);
        if (!newOwner.isOnline()) {
            player.sendMessage(color("&cPlayer is not online!"));
            return;
        }

        if (plot.getOwner() == newOwner) {
            player.sendMessage(color("&cYou cannot transfer this plot to yourself!"));
            return;
        }

        Player newOwnerPlayer = newOwner.getOnlinePlayer();
        if (!newOwnerPlayer.hasPermission("svplots.own.unlimited")) {
            List<SurvivalPlot> current = plugin.getPlotManager().getOwnedPlots(newOwnerPlayer);
            int max = 0;
            for (PermissionAttachmentInfo perm : newOwnerPlayer.getEffectivePermissions()) {
                if (perm.getPermission().startsWith("svplots.own.") && perm.getValue()) {
                    try {
                        int amount = Integer.parseInt(perm.getPermission().substring(perm.getPermission().lastIndexOf(".") + 1));
                        if (amount > max) {
                            max = amount;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (current.size() >= max) {
                player.sendMessage(color("&cPlayer cannot own any more plots!"));
                return;
            }
        }

        if (!new PlotTransferEvent(plot, newOwner).callEvent()) {
            SurvivalPlotsPlugin.getInst().info("PlotTransferEvent was cancelled, skipping transfer plot...");
            return;
        }

        plotPlayer.setConfirmation(() -> {
            WorldEditHook.backupPlot(plot, plot.getRawOwner());
            sender.sendMessage(color("&aSuccessfully backed-up plot " + plot.getId()));

            plot.setOwner(newOwner.getName());
            player.sendMessage(color("&aYou have transferred this plot's ownership to " + newOwner.getName()));
        });

        TextComponent component = new TextComponent(color("&eType &6/plot confirm &eto confirm transferring your plot."));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(color("&aClick to confirm"))));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot confirm"));
        player.sendMessage(component);
        player.sendMessage(color("&cWARNING!! Once transferred, you will not have access to the plot anymore!"));
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
