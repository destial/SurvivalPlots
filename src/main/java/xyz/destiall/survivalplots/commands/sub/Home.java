package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;

public class Home extends SubCommand {
    public Home() {
        super("user");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPlayer(sender))
            return;

        Player player = (Player) sender;

        List<SurvivalPlot> plots = plugin.getPlotManager().getOwnedPlots(player);
        if (plots.size() == 0) {
            player.sendMessage(color("&cYou have no plots!"));
            return;
        }

        SurvivalPlot plot = plots.get(0);
        if (args.length > 0) {
            try {
                plot = plots.get(Integer.parseInt(args[0]) - 1);
            } catch (Exception e) {
                player.sendMessage(color("&cInvalid plot number!"));
                return;
            }
        }

        try {
            player.teleportAsync(plot.getHome());
        } catch (Exception e) {
            player.teleport(plot.getHome());
        }

        player.sendMessage(Messages.Key.TELEPORT_HOME.get(player, plot));
    }
}
