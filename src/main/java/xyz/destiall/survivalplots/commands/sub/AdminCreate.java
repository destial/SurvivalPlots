package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.Arrays;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class AdminCreate extends SubCommand {
    public AdminCreate() {
        super("admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cYou need to be a player!"));
            return;
        }

        BoundingBox selection = WorldEditHook.getSelection((Player) sender);
        if (selection == null) {
            sender.sendMessage(color("&cYou do not have a complete selection!"));
            return;
        }

        boolean fullHeight = Arrays.stream(args).noneMatch(a -> a.equalsIgnoreCase("-s"));

        SurvivalPlot plot = plugin.getPlotManager().createPlot(((Player) sender).getWorld(), selection, fullHeight);

        sender.sendMessage(color("&aCreated plot " + plot.getId() + " in " + plot.getWorld().getName()));

        WorldEditHook.backupPlot(plot, "default");
    }
}
