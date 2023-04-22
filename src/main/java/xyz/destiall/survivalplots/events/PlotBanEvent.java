package xyz.destiall.survivalplots.events;

import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PlotBanEvent extends PlotEvent {
    private final PlotPlayer banning;
    public PlotBanEvent(SurvivalPlot plot, PlotPlayer banning) {
        super(plot);
        this.banning = banning;
    }

    public PlotPlayer getBanning() {
        return banning;
    }
}
