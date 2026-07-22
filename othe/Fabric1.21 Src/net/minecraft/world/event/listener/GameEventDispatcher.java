/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.event.listener;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;

/**
 * A game event dispatcher dispatches game events to its listeners.
 */
public interface GameEventDispatcher {
    /**
     * An unmodifiable, empty (non-operative) dispatcher.
     */
    public static final GameEventDispatcher EMPTY = new GameEventDispatcher(){

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void addListener(GameEventListener listener) {
        }

        @Override
        public void removeListener(GameEventListener listener) {
        }

        @Override
        public boolean dispatch(RegistryEntry<GameEvent> event, Vec3d pos, GameEvent.Emitter emitter, DispatchCallback callback) {
            return false;
        }
    };

    /**
     * Returns whether this dispatcher has no listeners.
     */
    public boolean isEmpty();

    /**
     * Adds a listener to this dispatcher.
     * 
     * @param listener the listener to add
     */
    public void addListener(GameEventListener var1);

    /**
     * Removes a listener from this dispatcher if it is present.
     * 
     * @param listener the listener to remove
     */
    public void removeListener(GameEventListener var1);

    /**
     * Dispatches an event to all the listeners in this dispatcher.
     * 
     * @param event the event
     */
    public boolean dispatch(RegistryEntry<GameEvent> var1, Vec3d var2, GameEvent.Emitter var3, DispatchCallback var4);

    @FunctionalInterface
    public static interface DispatchCallback {
        public void visit(GameEventListener var1, Vec3d var2);
    }
}

