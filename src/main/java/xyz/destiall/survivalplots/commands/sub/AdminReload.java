package xyz.destiall.survivalplots.commands.sub;

import org.bukkit.command.CommandSender;
import xyz.destiall.survivalplots.commands.SubCommand;

public class AdminReload extends SubCommand {
    public AdminReload() {
        super("admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(color("&aReloading SurvivalPlots..."));
        plugin.reload();
        sender.sendMessage(color("&aSuccessfully reloaded SurvivalPlots!"));
    }
}
