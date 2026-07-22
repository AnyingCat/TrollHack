package github.trollhack.events.impl;

import github.trollhack.events.Event;

public class JumpEvent extends Event {
    private final boolean pre;

    public JumpEvent(boolean pre) {
        super(pre ? Stage.Pre : Stage.Post);
        this.pre = pre;
    }

    public boolean isPre() {
        return pre;
    }
}
