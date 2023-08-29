package xyz.destiall.survivalplots.events;

import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PlotRestoreEvent extends PlotEvent {
    public PlotRestoreEvent(SurvivalPlot plot) {
        super(plot);
    }
}
