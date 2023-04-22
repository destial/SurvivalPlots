package xyz.destiall.survivalplots.events;

import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PlotCreateEvent extends PlotEvent {
    public PlotCreateEvent(SurvivalPlot plot) {
        super(plot);
    }
}
