package xyz.destiall.survivalplots;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.io.File;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class Messages {
    private static YamlConfiguration config;

    private Messages() {}

    public static void init(SurvivalPlotsPlugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "messages.yml");
        config = new YamlConfiguration();

        if (configFile.exists()) {
            try {
                config.load(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean newKeys = false;
            for (Key key : Key.values()) {
                if (config.contains(key.name().toLowerCase())) continue;

                config.set(key.name().toLowerCase(), key.getDefaultMessage());
                newKeys = true;
            }

            if (!newKeys)
                return;
        } else {
            for (Key key : Key.values()) {
                config.set(key.name().toLowerCase(), key.getDefaultMessage());
            }
        }

        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getMessage(Key key) {
        if (config == null)
            return color(key.getDefaultMessage());

        return color(config.getString(key.name().toLowerCase(), key.getDefaultMessage()));
    }

    private static String format(Key key, Player player, SurvivalPlot plot) {
        String message = getMessage(key);
        if (player != null)
            message = message.replace("{player}", player.getName());
        if (plot == null && player != null)
            plot = SurvivalPlotsPlugin.getInst().getPlotManager().getPlotAt(player.getLocation());

        message = message.replace("{plotmoney}", SurvivalPlotsPlugin.getInst().getEconomyManager().getCurrency());

        if (plot != null) {
            message = message.replace("{plotid}", String.valueOf(plot.getId()));
            message = message.replace("{plotowner}", plot.getRawOwner());
            message = message.replace("{plotcost}", plot.getOwner() == null ? String.valueOf(SurvivalPlotsPlugin.getInst().getEconomyManager().getPlotCost()) : "N/A");
            message = message.replace("{plotresetcost}", plot.getOwner() == null ? String.valueOf(SurvivalPlotsPlugin.getInst().getEconomyManager().getPlotReset()) : "N/A");
            message = message.replace("{plotmembers}", String.join(", ", plot.getMembers()));
            message = message.replace("{plotbanned}", String.join(", ", plot.getBanned()));
            message = message.replace("{plotdescription}", plot.getDescription());
            message = message.replace("{plotexpiry}", PlotUtils.relativeDate(plot.getExpiryDate()));
        }

        return message;
    }

    public enum Key {
        ENTER_OWNED_PLOT_TITLE("&6Plot Owner:"),
        ENTER_OWNED_PLOT_SUBTITLE("&6{plotowner}"),
        ENTER_AVAILABLE_PLOT_TITLE("&aPlot for sale!"),
        ENTER_AVAILABLE_PLOT_SUBTITLE("&6Cost: {plotcost} {plotmoney}"),
        BANNED_FROM_PLOT_TITLE("&cBanned!"),
        BANNED_FROM_PLOT_SUBTITLE("&c{plotowner} has banned you from this plot!"),
        TELEPORT_HOME("&6Teleported to plot"),
        TELEPORT_OTHERS("&6Teleported to {plotowner}'s plot"),
        NO_BUILD("&cYou do not have permission to build here!"),
        NO_INTERACT("&cYou do not have permission to interact here!"),
        NO_OPEN_INVENTORY("&cYou do not have permission to open this!"),
        NO_PVP("&cYou do not have permission to PvP here!"),
        NO_USE("&cYou do not have permission to use here!"),
        INFO("&6Plot Info:\n&6Owner: {plotowner}\n&6Members: {plotmembers}\n&6ID: {plotid}"),
        NO_PERMS_ON_PLOT("&cYou do not own this plot!"),
        NOT_STANDING_ON_PLOT("&cYou are not standing on a plot!"),
        NO_AVAILABLE_PLOTS("&cThere are no available plots!"),
        PLAYER_NOT_OWNED_BEFORE("&cPlayer has not owned this plot before!"),
        PLAYER_NOT_SAME("&cCurrent plot owner is not the same as restoring player!"),

        ;
        private final String defaultMessage;
        Key(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }

        public String get(Player player, SurvivalPlot plot) {
            return Messages.format(this, player, plot);
        }
    }
}
