/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.EntitySubPredicateTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record LightningBoltPredicate(NumberRange.IntRange blocksSetOnFire, Optional<EntityPredicate> entityStruck) implements EntitySubPredicate
{
    public static final MapCodec<LightningBoltPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(NumberRange.IntRange.CODEC.optionalFieldOf("blocks_set_on_fire", NumberRange.IntRange.ANY).forGetter(LightningBoltPredicate::blocksSetOnFire), EntityPredicate.CODEC.optionalFieldOf("entity_struck").forGetter(LightningBoltPredicate::entityStruck)).apply((Applicative<LightningBoltPredicate, ?>)instance, LightningBoltPredicate::new));

    public static LightningBoltPredicate of(NumberRange.IntRange blocksSetOnFire) {
        return new LightningBoltPredicate(blocksSetOnFire, Optional.empty());
    }

    public MapCodec<LightningBoltPredicate> getCodec() {
        return EntitySubPredicateTypes.LIGHTNING;
    }

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (!(entity instanceof LightningEntity)) {
            return false;
        }
        LightningEntity lightningEntity = (LightningEntity)entity;
        return this.blocksSetOnFire.test(lightningEntity.getBlocksSetOnFire()) && (this.entityStruck.isEmpty() || lightningEntity.getStruckEntities().anyMatch(struckEntity -> this.entityStruck.get().test(world, pos, (Entity)struckEntity)));
    }
}

