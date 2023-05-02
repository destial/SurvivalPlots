package xyz.destiall.survivalplots.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public abstract class PlotEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    @Override public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private final SurvivalPlot plot;

    public PlotEvent(SurvivalPlot plot) {
        this.plot = plot;
    }

    public PlotEvent(SurvivalPlot plot, boolean async) {
        super(async);
        this.plot = plot;
    }

    public SurvivalPlot getPlot() {
        return plot;
    }

    private boolean cancelled = false;
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public boolean callEvent() {
        Bukkit.getPluginManager().callEvent(this);
        return !isCancelled();
    }
}
