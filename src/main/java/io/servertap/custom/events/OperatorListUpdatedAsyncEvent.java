package io.servertap.custom.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class OperatorListUpdatedAsyncEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public OperatorListUpdatedAsyncEvent() {
        super(true);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
