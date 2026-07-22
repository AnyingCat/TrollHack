package github.trollhack.events.impl;

import github.trollhack.events.Event;

public class TravelEvent extends Event {
    private final boolean pre;

    public TravelEvent(boolean pre) {
        super(pre ? Stage.Pre : Stage.Post);
        this.pre = pre;
    }

    public boolean isPre() {
        return pre;
    }
}
