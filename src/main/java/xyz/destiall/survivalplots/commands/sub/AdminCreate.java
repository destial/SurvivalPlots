package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.Arrays;

public class AdminCreate extends SubCommand {
    public AdminCreate() {
        super("admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPlayer(sender))
            return;

        Player player = (Player) sender;
        BoundingBox selection = WorldEditHook.getSelection(player);
        if (selection == null) {
            player.sendMessage(color("&cYou do not have a complete selection!"));
            return;
        }

        boolean fullHeight = Arrays.stream(args).noneMatch(a -> a.equalsIgnoreCase("-s"));

        SurvivalPlot plot = plugin.getPlotManager().createPlot(player.getWorld(), selection, fullHeight);

        if (plot == null)
            return;

        player.sendMessage(color("&aCreated plot " + plot.getId() + " in " + plot.getWorld().getName()));
        WorldEditHook.backupPlot(plot, "default");
    }
}
