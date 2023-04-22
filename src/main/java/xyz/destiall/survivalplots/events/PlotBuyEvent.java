package xyz.destiall.survivalplots.events;

import org.bukkit.event.Cancellable;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PlotBuyEvent extends PlotEvent implements Cancellable {
    public PlotBuyEvent(SurvivalPlot plot) {
        super(plot);
    }
}
