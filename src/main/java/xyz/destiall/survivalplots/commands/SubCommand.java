package xyz.destiall.survivalplots.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SubCommand {
    protected final Permission permission;
    protected final SurvivalPlotsPlugin plugin;

    private static final List<Permission> ALL_PERMISSIONS = new ArrayList<>();

    public SubCommand(String permission) {
        plugin = SurvivalPlotsPlugin.getInst();
        this.permission = ALL_PERMISSIONS.stream().filter(p -> p.getName().equals("svplots." + permission)).findFirst().orElseGet(() -> {
            Permission p = new Permission("svplots." + permission);
            try {
                Bukkit.getServer().getPluginManager().addPermission(p);
            } catch (Exception ignored) {
                p = Bukkit.getServer().getPluginManager().getPermission(p.getName());
            }
            ALL_PERMISSIONS.add(p);
            return p;
        });
    }

    public Permission getPermission() {
        return permission;
    }

    public abstract void execute(CommandSender sender, String[] args);

    public List<String> tab(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
