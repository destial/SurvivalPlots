package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.hooks.WorldGuardHook;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        boolean setHome = Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("-h"));
        boolean createWg = Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("-wg"));

        SurvivalPlot plot = plugin.getPlotManager().createPlot(player.getWorld(), selection, fullHeight);

        if (plot == null)
            return;

        player.sendMessage(color("&aCreated plot " + plot.getId() + " in " + plot.getWorld().getName()));
        if (setHome) {
            plot.setHome(player.getLocation());
            player.sendMessage(color("&aSet plot home to current position"));
        }

        if (createWg) {
            if (WorldGuardHook.createRegion(plot) != null) {
                player.sendMessage(color("&aCreated a worldguard region for this plot"));
            } else {
                player.sendMessage(color("&cUnable to create a worldguard region for this plot"));
            }

            WorldGuardHook.saveRegionManager(plot.getWorld());
        }
        WorldEditHook.backupPlot(plot, "default");
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        if (args.length == 0)
            return super.tab(sender, args);

        return Stream.of("-h", "-s", "-wg")
                .filter(a -> a.startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
