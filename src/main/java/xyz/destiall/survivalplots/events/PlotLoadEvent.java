package xyz.destiall.survivalplots.events;

import org.bukkit.event.Cancellable;
import xyz.destiall.survivalplots.plot.Schematic;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PlotLoadEvent extends PlotEvent implements Cancellable {
    private final Schematic schematic;
    public PlotLoadEvent(SurvivalPlot plot, Schematic schematic) {
        super(plot, true);
        this.schematic = schematic;
    }

    public Schematic getSchematic() {
        return schematic;
    }
}
