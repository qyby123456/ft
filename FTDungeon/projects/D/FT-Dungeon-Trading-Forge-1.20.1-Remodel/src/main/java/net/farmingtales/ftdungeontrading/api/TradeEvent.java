package net.farmingtales.ftdungeontrading.api;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public abstract class TradeEvent extends Event {
    private final TradeContext context;
    private final TradeOffer offer;

    protected TradeEvent(TradeContext context, TradeOffer offer) {
        this.context = context;
        this.offer = offer;
    }

    public TradeContext context() { return context; }
    public TradeOffer offer() { return offer; }

    @Cancelable
    public static final class BeforePurchase extends TradeEvent {
        public BeforePurchase(TradeContext context, TradeOffer offer) { super(context, offer); }
    }

    public static final class Purchased extends TradeEvent {
        private final int remainingStock;

        public Purchased(TradeContext context, TradeOffer offer, int remainingStock) {
            super(context, offer);
            this.remainingStock = remainingStock;
        }

        public int remainingStock() { return remainingStock; }
    }
}
