package github.trollhack.events.impl;

import github.trollhack.events.Event;

public class TickEvent extends Event {
    public static final TickEvent INSTANCE = new TickEvent();

    private TickEvent() {}
}
