/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface Leashable {
    public static final String LEASH_NBT_KEY = "leash";
    public static final double field_52314 = 10.0;
    public static final double field_52315 = 6.0;

    @Nullable
    public LeashData getLeashData();

    public void setLeashData(@Nullable LeashData var1);

    default public boolean isLeashed() {
        return this.getLeashData() != null && this.getLeashData().leashHolder != null;
    }

    default public boolean mightBeLeashed() {
        return this.getLeashData() != null;
    }

    default public boolean canLeashAttachTo() {
        return this.canBeLeashed() && !this.isLeashed();
    }

    default public boolean canBeLeashed() {
        return true;
    }

    default public void setUnresolvedLeashHolderId(int unresolvedLeashHolderId) {
        this.setLeashData(new LeashData(unresolvedLeashHolderId));
        Leashable.detachLeash((Entity)((Object)this), false, false);
    }

    @Nullable
    default public LeashData readLeashDataFromNbt(NbtCompound nbt) {
        Either either;
        if (nbt.contains(LEASH_NBT_KEY, NbtElement.COMPOUND_TYPE)) {
            return new LeashData(Either.left(nbt.getCompound(LEASH_NBT_KEY).getUuid("UUID")));
        }
        if (nbt.contains(LEASH_NBT_KEY, NbtElement.INT_ARRAY_TYPE) && (either = (Either)NbtHelper.toBlockPos(nbt, LEASH_NBT_KEY).map(Either::right).orElse(null)) != null) {
            return new LeashData(either);
        }
        return null;
    }

    default public void writeLeashDataToNbt(NbtCompound nbt, @Nullable LeashData leashData) {
        if (leashData == null) {
            return;
        }
        Either<UUID, BlockPos> either = leashData.unresolvedLeashData;
        Entity entity = leashData.leashHolder;
        if (entity instanceof LeashKnotEntity) {
            LeashKnotEntity leashKnotEntity = (LeashKnotEntity)entity;
            either = Either.right(leashKnotEntity.getAttachedBlockPos());
        } else if (leashData.leashHolder != null) {
            either = Either.left(leashData.leashHolder.getUuid());
        }
        if (either == null) {
            return;
        }
        nbt.put(LEASH_NBT_KEY, either.map(uuid -> {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putUuid("UUID", (UUID)uuid);
            return nbtCompound;
        }, NbtHelper::fromBlockPos));
    }

    private static <E extends Entity> void resolveLeashData(E entity, LeashData leashData) {
        World world;
        if (leashData.unresolvedLeashData != null && (world = entity.getWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            Optional<UUID> optional = leashData.unresolvedLeashData.left();
            Optional<BlockPos> optional2 = leashData.unresolvedLeashData.right();
            if (optional.isPresent()) {
                Entity entity2 = serverWorld.getEntity(optional.get());
                if (entity2 != null) {
                    Leashable.attachLeash(entity, entity2, true);
                    return;
                }
            } else if (optional2.isPresent()) {
                Leashable.attachLeash(entity, LeashKnotEntity.getOrCreate(serverWorld, optional2.get()), true);
                return;
            }
            if (entity.age > 100) {
                entity.dropItem(Items.LEAD);
                ((Leashable)((Object)entity)).setLeashData(null);
            }
        }
    }

    default public void detachLeash(boolean sendPacket, boolean dropItem) {
        Leashable.detachLeash((Entity)((Object)this), sendPacket, dropItem);
    }

    private static <E extends Entity> void detachLeash(E entity, boolean sendPacket, boolean dropItem) {
        LeashData leashData = ((Leashable)((Object)entity)).getLeashData();
        if (leashData != null && leashData.leashHolder != null) {
            World world;
            ((Leashable)((Object)entity)).setLeashData(null);
            if (!entity.getWorld().isClient && dropItem) {
                entity.dropItem(Items.LEAD);
            }
            if (sendPacket && (world = entity.getWorld()) instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                serverWorld.getChunkManager().sendToOtherNearbyPlayers(entity, new EntityAttachS2CPacket(entity, null));
            }
        }
    }

    public static <E extends Entity> void tickLeash(E entity) {
        Entity entity2;
        LeashData leashData = ((Leashable)((Object)entity)).getLeashData();
        if (leashData != null && leashData.unresolvedLeashData != null) {
            Leashable.resolveLeashData(entity, leashData);
        }
        if (leashData == null || leashData.leashHolder == null) {
            return;
        }
        if (!entity.isAlive() || !leashData.leashHolder.isAlive()) {
            Leashable.detachLeash(entity, true, true);
        }
        if ((entity2 = ((Leashable)((Object)entity)).getLeashHolder()) != null && entity2.getWorld() == entity.getWorld()) {
            float f = entity.distanceTo(entity2);
            if (!((Leashable)((Object)entity)).shouldTickLeash(entity2, f)) {
                return;
            }
            if ((double)f > 10.0) {
                ((Leashable)((Object)entity)).detachLeash();
            } else if ((double)f > 6.0) {
                ((Leashable)((Object)entity)).applyLeashElasticity(entity2, f);
                entity.limitFallDistance();
            } else {
                ((Leashable)((Object)entity)).onShortLeashTick(entity2);
            }
        }
    }

    default public boolean shouldTickLeash(Entity leashHolder, float distance) {
        return true;
    }

    default public void detachLeash() {
        this.detachLeash(true, true);
    }

    default public void onShortLeashTick(Entity entity) {
    }

    default public void applyLeashElasticity(Entity leashHolder, float distance) {
        Leashable.applyLeashElasticity((Entity)((Object)this), leashHolder, distance);
    }

    private static <E extends Entity> void applyLeashElasticity(E entity, Entity leashHolder, float distance) {
        double d = (leashHolder.getX() - entity.getX()) / (double)distance;
        double e = (leashHolder.getY() - entity.getY()) / (double)distance;
        double f = (leashHolder.getZ() - entity.getZ()) / (double)distance;
        entity.setVelocity(entity.getVelocity().add(Math.copySign(d * d * 0.4, d), Math.copySign(e * e * 0.4, e), Math.copySign(f * f * 0.4, f)));
    }

    default public void attachLeash(Entity leashHolder, boolean sendPacket) {
        Leashable.attachLeash((Entity)((Object)this), leashHolder, sendPacket);
    }

    private static <E extends Entity> void attachLeash(E entity, Entity leashHolder, boolean sendPacket) {
        World world;
        LeashData leashData = ((Leashable)((Object)entity)).getLeashData();
        if (leashData == null) {
            leashData = new LeashData(leashHolder);
            ((Leashable)((Object)entity)).setLeashData(leashData);
        } else {
            leashData.setLeashHolder(leashHolder);
        }
        if (sendPacket && (world = entity.getWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            serverWorld.getChunkManager().sendToOtherNearbyPlayers(entity, new EntityAttachS2CPacket(entity, leashHolder));
        }
        if (entity.hasVehicle()) {
            entity.stopRiding();
        }
    }

    @Nullable
    default public Entity getLeashHolder() {
        return Leashable.getLeashHolder((Entity)((Object)this));
    }

    @Nullable
    private static <E extends Entity> Entity getLeashHolder(E entity) {
        Entity entity2;
        LeashData leashData = ((Leashable)((Object)entity)).getLeashData();
        if (leashData == null) {
            return null;
        }
        if (leashData.unresolvedLeashHolderId != 0 && entity.getWorld().isClient && (entity2 = entity.getWorld().getEntityById(leashData.unresolvedLeashHolderId)) instanceof Entity) {
            Entity entity22 = entity2;
            leashData.setLeashHolder(entity22);
        }
        return leashData.leashHolder;
    }

    public static final class LeashData {
        int unresolvedLeashHolderId;
        @Nullable
        public Entity leashHolder;
        @Nullable
        public Either<UUID, BlockPos> unresolvedLeashData;

        LeashData(Either<UUID, BlockPos> unresolvedLeashData) {
            this.unresolvedLeashData = unresolvedLeashData;
        }

        LeashData(Entity leashHolder) {
            this.leashHolder = leashHolder;
        }

        LeashData(int unresolvedLeashHolderId) {
            this.unresolvedLeashHolderId = unresolvedLeashHolderId;
        }

        public void setLeashHolder(Entity leashHolder) {
            this.leashHolder = leashHolder;
            this.unresolvedLeashData = null;
            this.unresolvedLeashHolderId = 0;
        }
    }
}

