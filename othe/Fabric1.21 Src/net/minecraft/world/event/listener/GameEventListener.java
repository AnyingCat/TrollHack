/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.event.listener;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;

/**
 * A game event listener listens to game events from {@link GameEventDispatcher}s.
 */
public interface GameEventListener {
    /**
     * Returns the position source of this listener.
     */
    public PositionSource getPositionSource();

    /**
     * Returns the range, in blocks, of the listener.
     */
    public int getRange();

    /**
     * Listens to an incoming game event.
     * 
     * @return {@code true} if the game event has been accepted by this listener
     */
    public boolean listen(ServerWorld var1, RegistryEntry<GameEvent> var2, GameEvent.Emitter var3, Vec3d var4);

    default public TriggerOrder getTriggerOrder() {
        return TriggerOrder.UNSPECIFIED;
    }

    public static enum TriggerOrder {
        UNSPECIFIED,
        BY_DISTANCE;

    }

    public static interface Holder<T extends GameEventListener> {
        public T getEventListener();
    }
}

