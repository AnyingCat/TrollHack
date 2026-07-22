/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public record SummonEntityEnchantmentEffect(RegistryEntryList<EntityType<?>> entityTypes, boolean joinTeam) implements EnchantmentEntityEffect
{
    public static final MapCodec<SummonEntityEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryCodecs.entryList(RegistryKeys.ENTITY_TYPE).fieldOf("entity")).forGetter(SummonEntityEnchantmentEffect::entityTypes), Codec.BOOL.optionalFieldOf("join_team", false).forGetter(SummonEntityEnchantmentEffect::joinTeam)).apply((Applicative<SummonEntityEnchantmentEffect, ?>)instance, SummonEntityEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos);
        if (!World.isValid(blockPos)) {
            return;
        }
        Optional<RegistryEntry<EntityType<?>>> optional = this.entityTypes().getRandom(world.getRandom());
        if (optional.isEmpty()) {
            return;
        }
        Object entity = optional.get().value().spawn(world, blockPos, SpawnReason.TRIGGERED);
        if (entity == null) {
            return;
        }
        if (entity instanceof LightningEntity) {
            LightningEntity lightningEntity = (LightningEntity)entity;
            LivingEntity livingEntity = context.owner();
            if (livingEntity instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)livingEntity;
                lightningEntity.setChanneler(serverPlayerEntity);
            }
        }
        if (this.joinTeam && user.getScoreboardTeam() != null) {
            world.getScoreboard().addScoreHolderToTeam(((Entity)entity).getNameForScoreboard(), user.getScoreboardTeam());
        }
        ((Entity)entity).refreshPositionAndAngles(pos.x, pos.y, pos.z, ((Entity)entity).getYaw(), ((Entity)entity).getPitch());
    }

    public MapCodec<SummonEntityEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

