package xyz.destiall.survivalplots.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.commands.sub.AdminCreate;
import xyz.destiall.survivalplots.commands.sub.AdminDelete;
import xyz.destiall.survivalplots.commands.sub.Ban;
import xyz.destiall.survivalplots.commands.sub.Buy;
import xyz.destiall.survivalplots.commands.sub.Confirm;
import xyz.destiall.survivalplots.commands.sub.Flags;
import xyz.destiall.survivalplots.commands.sub.Reset;
import xyz.destiall.survivalplots.commands.sub.Sell;
import xyz.destiall.survivalplots.commands.sub.Trust;
import xyz.destiall.survivalplots.commands.sub.Unban;
import xyz.destiall.survivalplots.commands.sub.Untrust;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlotCommand implements CommandExecutor, TabExecutor {
    private final SurvivalPlotsPlugin plugin;
    private final Map<String, SubCommand> subCommands;

    public PlotCommand(SurvivalPlotsPlugin plugin) {
        this.plugin = plugin;
        subCommands = new HashMap<>();

        subCommands.put("create", new AdminCreate());
        subCommands.put("delete", new AdminDelete());

        subCommands.put("ban", new Ban());
        subCommands.put("buy", new Buy());
        subCommands.put("confirm", new Confirm());
        subCommands.put("flags", new Flags());
        subCommands.put("reset", new Reset());
        subCommands.put("sell", new Sell());
        subCommands.put("trust", new Trust());
        subCommands.put("untrust", new Untrust());
        subCommands.put("unban", new Unban());
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0)
            return false;

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            sender.sendMessage(color("&cSub-command not found!"));
            return false;
        }

        if (!sender.hasPermission(sub.getPermission())) {
            sender.sendMessage(color("&cYou do not have permission!"));
            return false;
        }

        sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.entrySet().stream()
                    .filter(s -> sender.hasPermission(s.getValue().getPermission()))
                    .map(Map.Entry::getKey)
                    .filter(k -> k.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            return Collections.emptyList();
        }

        return sub.tab(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
