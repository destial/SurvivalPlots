package xyz.destiall.survivalplots.events;

import org.bukkit.event.Cancellable;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PlotExpireEvent extends PlotEvent implements Cancellable {
    public PlotExpireEvent(SurvivalPlot plot) {
        super(plot, true);
    }
}
