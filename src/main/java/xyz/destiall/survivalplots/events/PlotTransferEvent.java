package xyz.destiall.survivalplots.events;

import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

public class PlotTransferEvent extends PlotEvent {
    private final PlotPlayer newOwner;
    public PlotTransferEvent(SurvivalPlot plot, PlotPlayer transfer) {
        super(plot);
        this.newOwner = transfer;
    }

    public PlotPlayer getNewOwner() {
        return newOwner;
    }
}
