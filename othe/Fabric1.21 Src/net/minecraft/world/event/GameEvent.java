/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.event;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

public record GameEvent(int notificationRadius) {
    public static final RegistryEntry.Reference<GameEvent> BLOCK_ACTIVATE = GameEvent.register("block_activate");
    public static final RegistryEntry.Reference<GameEvent> BLOCK_ATTACH = GameEvent.register("block_attach");
    public static final RegistryEntry.Reference<GameEvent> BLOCK_CHANGE = GameEvent.register("block_change");
    public static final RegistryEntry.Reference<GameEvent> BLOCK_CLOSE = GameEvent.register("block_close");
    public static final RegistryEntry.Reference<GameEvent> BLOCK_DEACTIVATE = GameEvent.register("block_deactivate");
    public static final RegistryEntry.Reference<GameEvent> BLOCK_DESTROY = GameEvent.register("block_destroy");
    public static final RegistryEntry.Reference<GameEvent> BLOCK_DETACH = GameEvent.register("block_detach");
    public static final RegistryEntry.Reference<GameEvent> BLOCK_OPEN = GameEvent.register("block_open");
    public static final RegistryEntry.Reference<GameEvent> BLOCK_PLACE = GameEvent.register("block_place");
    public static final RegistryEntry.Reference<GameEvent> CONTAINER_CLOSE = GameEvent.register("container_close");
    public static final RegistryEntry.Reference<GameEvent> CONTAINER_OPEN = GameEvent.register("container_open");
    public static final RegistryEntry.Reference<GameEvent> DRINK = GameEvent.register("drink");
    public static final RegistryEntry.Reference<GameEvent> EAT = GameEvent.register("eat");
    public static final RegistryEntry.Reference<GameEvent> ELYTRA_GLIDE = GameEvent.register("elytra_glide");
    public static final RegistryEntry.Reference<GameEvent> ENTITY_DAMAGE = GameEvent.register("entity_damage");
    public static final RegistryEntry.Reference<GameEvent> ENTITY_DIE = GameEvent.register("entity_die");
    public static final RegistryEntry.Reference<GameEvent> ENTITY_DISMOUNT = GameEvent.register("entity_dismount");
    public static final RegistryEntry.Reference<GameEvent> ENTITY_INTERACT = GameEvent.register("entity_interact");
    public static final RegistryEntry.Reference<GameEvent> ENTITY_MOUNT = GameEvent.register("entity_mount");
    public static final RegistryEntry.Reference<GameEvent> ENTITY_PLACE = GameEvent.register("entity_place");
    public static final RegistryEntry.Reference<GameEvent> ENTITY_ACTION = GameEvent.register("entity_action");
    public static final RegistryEntry.Reference<GameEvent> EQUIP = GameEvent.register("equip");
    public static final RegistryEntry.Reference<GameEvent> EXPLODE = GameEvent.register("explode");
    public static final RegistryEntry.Reference<GameEvent> FLAP = GameEvent.register("flap");
    public static final RegistryEntry.Reference<GameEvent> FLUID_PICKUP = GameEvent.register("fluid_pickup");
    public static final RegistryEntry.Reference<GameEvent> FLUID_PLACE = GameEvent.register("fluid_place");
    public static final RegistryEntry.Reference<GameEvent> HIT_GROUND = GameEvent.register("hit_ground");
    public static final RegistryEntry.Reference<GameEvent> INSTRUMENT_PLAY = GameEvent.register("instrument_play");
    public static final RegistryEntry.Reference<GameEvent> ITEM_INTERACT_FINISH = GameEvent.register("item_interact_finish");
    public static final RegistryEntry.Reference<GameEvent> ITEM_INTERACT_START = GameEvent.register("item_interact_start");
    public static final RegistryEntry.Reference<GameEvent> JUKEBOX_PLAY = GameEvent.register("jukebox_play", 10);
    public static final RegistryEntry.Reference<GameEvent> JUKEBOX_STOP_PLAY = GameEvent.register("jukebox_stop_play", 10);
    public static final RegistryEntry.Reference<GameEvent> LIGHTNING_STRIKE = GameEvent.register("lightning_strike");
    public static final RegistryEntry.Reference<GameEvent> NOTE_BLOCK_PLAY = GameEvent.register("note_block_play");
    public static final RegistryEntry.Reference<GameEvent> PRIME_FUSE = GameEvent.register("prime_fuse");
    public static final RegistryEntry.Reference<GameEvent> PROJECTILE_LAND = GameEvent.register("projectile_land");
    public static final RegistryEntry.Reference<GameEvent> PROJECTILE_SHOOT = GameEvent.register("projectile_shoot");
    public static final RegistryEntry.Reference<GameEvent> SCULK_SENSOR_TENDRILS_CLICKING = GameEvent.register("sculk_sensor_tendrils_clicking");
    public static final RegistryEntry.Reference<GameEvent> SHEAR = GameEvent.register("shear");
    public static final RegistryEntry.Reference<GameEvent> SHRIEK = GameEvent.register("shriek", 32);
    public static final RegistryEntry.Reference<GameEvent> SPLASH = GameEvent.register("splash");
    public static final RegistryEntry.Reference<GameEvent> STEP = GameEvent.register("step");
    public static final RegistryEntry.Reference<GameEvent> SWIM = GameEvent.register("swim");
    public static final RegistryEntry.Reference<GameEvent> TELEPORT = GameEvent.register("teleport");
    public static final RegistryEntry.Reference<GameEvent> UNEQUIP = GameEvent.register("unequip");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_1 = GameEvent.register("resonate_1");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_2 = GameEvent.register("resonate_2");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_3 = GameEvent.register("resonate_3");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_4 = GameEvent.register("resonate_4");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_5 = GameEvent.register("resonate_5");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_6 = GameEvent.register("resonate_6");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_7 = GameEvent.register("resonate_7");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_8 = GameEvent.register("resonate_8");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_9 = GameEvent.register("resonate_9");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_10 = GameEvent.register("resonate_10");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_11 = GameEvent.register("resonate_11");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_12 = GameEvent.register("resonate_12");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_13 = GameEvent.register("resonate_13");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_14 = GameEvent.register("resonate_14");
    public static final RegistryEntry.Reference<GameEvent> RESONATE_15 = GameEvent.register("resonate_15");
    public static final int DEFAULT_RANGE = 16;
    public static final Codec<RegistryEntry<GameEvent>> CODEC = RegistryFixedCodec.of(RegistryKeys.GAME_EVENT);

    public static RegistryEntry<GameEvent> registerAndGetDefault(Registry<GameEvent> registry) {
        return BLOCK_ACTIVATE;
    }

    private static RegistryEntry.Reference<GameEvent> register(String id) {
        return GameEvent.register(id, 16);
    }

    private static RegistryEntry.Reference<GameEvent> register(String id, int range) {
        return Registry.registerReference(Registries.GAME_EVENT, Identifier.ofVanilla(id), new GameEvent(range));
    }

    public static final class Message
    implements Comparable<Message> {
        private final RegistryEntry<GameEvent> event;
        private final Vec3d emitterPos;
        private final Emitter emitter;
        private final GameEventListener listener;
        private final double distanceTraveled;

        public Message(RegistryEntry<GameEvent> event, Vec3d emitterPos, Emitter emitter, GameEventListener listener, Vec3d listenerPos) {
            this.event = event;
            this.emitterPos = emitterPos;
            this.emitter = emitter;
            this.listener = listener;
            this.distanceTraveled = emitterPos.squaredDistanceTo(listenerPos);
        }

        @Override
        public int compareTo(Message message) {
            return Double.compare(this.distanceTraveled, message.distanceTraveled);
        }

        public RegistryEntry<GameEvent> getEvent() {
            return this.event;
        }

        public Vec3d getEmitterPos() {
            return this.emitterPos;
        }

        public Emitter getEmitter() {
            return this.emitter;
        }

        public GameEventListener getListener() {
            return this.listener;
        }

        @Override
        public /* synthetic */ int compareTo(Object other) {
            return this.compareTo((Message)other);
        }
    }

    public record Emitter(@Nullable Entity sourceEntity, @Nullable BlockState affectedState) {
        public static Emitter of(@Nullable Entity sourceEntity) {
            return new Emitter(sourceEntity, null);
        }

        public static Emitter of(@Nullable BlockState affectedState) {
            return new Emitter(null, affectedState);
        }

        public static Emitter of(@Nullable Entity sourceEntity, @Nullable BlockState affectedState) {
            return new Emitter(sourceEntity, affectedState);
        }

        @Nullable
        public Entity sourceEntity() {
            return this.sourceEntity;
        }

        @Nullable
        public BlockState affectedState() {
            return this.affectedState;
        }
    }
}

