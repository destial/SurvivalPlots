package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.player.PlotPlayer;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Confirm extends SubCommand {
    public Confirm() {
        super("user");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cYou need to be a player!"));
            return;
        }

        PlotPlayer player = plugin.getPlotPlayerManager().getPlotPlayer((Player) sender);
        if (!player.executeConfirmation()) {
            sender.sendMessage(color("&cYou have nothing to confirm!"));
        }
    }
}
