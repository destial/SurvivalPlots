package xyz.destiall.survivalplots.events;

import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PlotResetEvent extends PlotEvent {
    public PlotResetEvent(SurvivalPlot plot) {
        super(plot);
    }
}
