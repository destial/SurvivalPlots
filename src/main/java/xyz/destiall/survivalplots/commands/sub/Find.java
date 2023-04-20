package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.List;

public class Find extends SubCommand {
    public Find() {
        super("user.find");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPlayer(sender))
            return;

        Player player = (Player) sender;
        List<SurvivalPlot> plots = plugin.getPlotManager().getAvailablePlots();
        if (plots.size() == 0) {
            player.sendMessage(Messages.Key.NO_AVAILABLE_PLOTS.get(player, null));
            return;
        }

        SurvivalPlot plot = plots.get(0);

        try {
            player.teleportAsync(plot.getHome());
        } catch (Exception e) {
            player.teleport(plot.getHome());
        }

        player.sendMessage(color("Teleported to plot " + plot.getId()));
    }
}
