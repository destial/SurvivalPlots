package xyz.destiall.survivalplots.events;

import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PlotDeleteEvent extends PlotEvent {
    public PlotDeleteEvent(SurvivalPlot plot) {
        super(plot);
    }
}
