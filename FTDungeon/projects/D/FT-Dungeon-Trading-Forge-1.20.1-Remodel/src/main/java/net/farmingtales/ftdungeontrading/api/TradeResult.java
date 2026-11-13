package net.farmingtales.ftdungeontrading.api;

import net.minecraft.network.chat.Component;

public record TradeResult(Status status, Component message) {
    public enum Status {
        PREVIEW, PURCHASED, TOO_SOON, INSUFFICIENT_PAYMENT, INVENTORY_FULL,
        SOLD_OUT, MISSING_OFFER, QUOTE_CHANGED, UNAVAILABLE, DENIED, CANCELED
    }
}
