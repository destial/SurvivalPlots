package xyz.destiall.survivalplots.player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import xyz.destiall.survivalplots.hooks.PartiesHook;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import java.util.Timer;
import java.util.TimerTask;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class PlotPlayer {
    protected String name;
    protected Runnable confirmation;
    protected Timer confirmationTimer;

    PlotPlayer(String name) {
        this.name = name;
    }

    public boolean isOnline() {
        return getPlayer().isOnline();
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(name);
    }

    public boolean isOwner(SurvivalPlot plot) {
        return equals(plot.getOwner());
    }

    public String getName() {
        return name;
    }

    public boolean isMember(SurvivalPlot plot) {
        if (plot.hasFlag(PlotFlags.PARTY_TRUST) && PartiesHook.sameParty(plot.getOwner().getPlayer(), getPlayer()))
            return true;
        return plot.getMembers().contains(name);
    }

    public boolean isBanned(SurvivalPlot plot) {
        return plot.getBanned().contains(name);
    }

    public boolean canBuild(SurvivalPlot plot) {
        if (hasBypass())
            return true;

        if (isBanned(plot))
            return false;

        return isOwner(plot) || (isMember(plot) && plot.hasFlag(PlotFlags.MEMBER_BUILD)) || plot.hasFlag(PlotFlags.GUEST_BUILD);
    }

    public boolean canInteractBlock(SurvivalPlot plot) {
        if (hasBypass())
            return true;

        if (isBanned(plot))
            return false;

        return isOwner(plot) || (isMember(plot) && plot.hasFlag(PlotFlags.MEMBER_INTERACT_BLOCK)) || plot.hasFlag(PlotFlags.GUEST_INTERACT_BLOCK);
    }

    public boolean canInteractEntity(SurvivalPlot plot) {
        if (hasBypass())
            return true;

        if (isBanned(plot))
            return false;

        return isOwner(plot) || (isMember(plot) && plot.hasFlag(PlotFlags.MEMBER_INTERACT_ENTITY)) || plot.hasFlag(PlotFlags.GUEST_INTERACT_ENTITY);
    }

    public boolean canOpenInventory(SurvivalPlot plot) {
        if (hasBypass())
            return true;

        if (isBanned(plot))
            return false;

        return isOwner(plot) || (isMember(plot) && plot.hasFlag(PlotFlags.MEMBER_OPEN_INVENTORY)) || plot.hasFlag(PlotFlags.GUEST_OPEN_INVENTORY);
    }

    public boolean hasBypass() {
        OfflinePlayer of = getPlayer();
        if (!of.isOnline())
            return false;

        Player p = (Player) of;
        return p.hasPermission("svplots.bypass");
    }

    public boolean executeConfirmation() {
        if (confirmation == null)
            return false;

        confirmationTimer.cancel();
        confirmationTimer = null;

        confirmation.run();
        confirmation = null;
        return true;
    }

    public void setConfirmation(Runnable confirmation) {
        this.confirmation = confirmation;

        if (confirmationTimer != null) {
            confirmationTimer.cancel();
        }

        confirmationTimer = new Timer();
        confirmationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                OfflinePlayer player = getPlayer();
                if (player instanceof Player) {
                    ((Player) player).sendMessage(color("&cYour confirmation has expired!"));
                }

                PlotPlayer.this.confirmationTimer = null;
                PlotPlayer.this.confirmation = null;
            }
        }, 30 * 1000);

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (obj instanceof PlotPlayer)
            return this.name.equals(((PlotPlayer) obj).name);

        return false;
    }
}
