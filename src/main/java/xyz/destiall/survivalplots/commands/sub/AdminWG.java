package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import xyz.destiall.survivalplots.commands.SubCommand;
import xyz.destiall.survivalplots.hooks.WorldGuardHook;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdminWG extends SubCommand {
    public AdminWG() {
        super("admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(color("&cUsage: /plots adminwg [create/delete] [id]"));
            return;
        }

        String type = args[0];
        Collection<SurvivalPlot> plots = plugin.getPlotManager().getAllPlots();

        int id = -1;
        if (args.length > 1) {
            try {
                id = Integer.parseInt(args[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (type.equalsIgnoreCase("create")) {
            if (id == -1) {
                for (SurvivalPlot plot : plots) {
                    if (WorldGuardHook.createRegion(plot) == null) {
                        sender.sendMessage(color("&cUnable to create a worldguard region in plot " + plot.getId()));
                    }
                }
                plots.stream().map(SurvivalPlot::getWorld).distinct().forEach(WorldGuardHook::saveRegionManager);
                sender.sendMessage(color("&aCreated a worldguard region for all plots"));
                return;
            }

            final int finalId = id;
            SurvivalPlot plot = plots.stream().filter(p -> p.getId() == finalId).findFirst().orElse(null);
            if (plot == null) {
                sender.sendMessage(color("&cUnable to find plot id " + finalId));
                return;
            }

            if (WorldGuardHook.createRegion(plot) == null) {
                sender.sendMessage(color("&cUnable to create a worldguard region in plot " + plot.getId()));
                return;
            }

            WorldGuardHook.saveRegionManager(plot.getWorld());
            sender.sendMessage(color("&aCreated a worldguard region for plot " + plot.getId()));
            return;
        }

        if (type.equalsIgnoreCase("delete")) {
            if (id == -1) {
                for (SurvivalPlot plot : plots) {
                    WorldGuardHook.removeRegion(plot);
                }
                plots.stream().map(SurvivalPlot::getWorld).distinct().forEach(WorldGuardHook::saveRegionManager);
                sender.sendMessage(color("&aRemoved the worldguard region for all plots"));
                return;
            }

            final int finalId = id;
            SurvivalPlot plot = plots.stream().filter(p -> p.getId() == finalId).findFirst().orElse(null);
            if (plot == null) {
                sender.sendMessage(color("&cUnable to find plot id " + finalId));
                return;
            }

            WorldGuardHook.removeRegion(plot);
            WorldGuardHook.saveRegionManager(plot.getWorld());
            sender.sendMessage(color("&aRemoved the worldguard region for plot " + plot.getId()));
        }
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        if (args.length != 1)
            return super.tab(sender, args);

        return Stream.of("create", "delete")
                .filter(a -> a.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }
}
