package xyz.destiall.survivalplots.events;

import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PlotTrustEvent extends PlotEvent {
    private final PlotPlayer trusting;
    public PlotTrustEvent(SurvivalPlot plot, PlotPlayer other) {
        super(plot);
        this.trusting = other;
    }

    public PlotPlayer getTrusting() {
        return trusting;
    }
}
