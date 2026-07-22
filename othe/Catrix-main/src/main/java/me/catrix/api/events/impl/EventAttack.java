package me.catrix.api.events.impl;

import net.minecraft.entity.Entity;
import me.catrix.api.events.Event;

public class EventAttack extends Event {
    private final Entity entity;
    private boolean pre;
    private boolean cancelled;

    public EventAttack(Entity entity, boolean pre) {
        super(pre ? Stage.Pre : Stage.Post);
        this.entity = entity;
        this.pre = pre;
        this.cancelled = false;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isPre() {
        return pre;
    }

    public void cancel() {
        setCancelled(true);
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}