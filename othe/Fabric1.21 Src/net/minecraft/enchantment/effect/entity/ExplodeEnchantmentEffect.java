/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.AdvancedExplosionBehavior;
import org.jetbrains.annotations.Nullable;

public record ExplodeEnchantmentEffect(boolean attributeToUser, Optional<RegistryEntry<DamageType>> damageType, Optional<EnchantmentLevelBasedValue> knockbackMultiplier, Optional<RegistryEntryList<Block>> immuneBlocks, Vec3d offset, EnchantmentLevelBasedValue radius, boolean createFire, World.ExplosionSourceType blockInteraction, ParticleEffect smallParticle, ParticleEffect largeParticle, RegistryEntry<SoundEvent> sound) implements EnchantmentEntityEffect
{
    public static final MapCodec<ExplodeEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.BOOL.optionalFieldOf("attribute_to_user", false).forGetter(ExplodeEnchantmentEffect::attributeToUser), DamageType.ENTRY_CODEC.optionalFieldOf("damage_type").forGetter(ExplodeEnchantmentEffect::damageType), EnchantmentLevelBasedValue.CODEC.optionalFieldOf("knockback_multiplier").forGetter(ExplodeEnchantmentEffect::knockbackMultiplier), RegistryCodecs.entryList(RegistryKeys.BLOCK).optionalFieldOf("immune_blocks").forGetter(ExplodeEnchantmentEffect::immuneBlocks), Vec3d.CODEC.optionalFieldOf("offset", Vec3d.ZERO).forGetter(ExplodeEnchantmentEffect::offset), ((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("radius")).forGetter(ExplodeEnchantmentEffect::radius), Codec.BOOL.optionalFieldOf("create_fire", false).forGetter(ExplodeEnchantmentEffect::createFire), ((MapCodec)World.ExplosionSourceType.CODEC.fieldOf("block_interaction")).forGetter(ExplodeEnchantmentEffect::blockInteraction), ((MapCodec)ParticleTypes.TYPE_CODEC.fieldOf("small_particle")).forGetter(ExplodeEnchantmentEffect::smallParticle), ((MapCodec)ParticleTypes.TYPE_CODEC.fieldOf("large_particle")).forGetter(ExplodeEnchantmentEffect::largeParticle), ((MapCodec)SoundEvent.ENTRY_CODEC.fieldOf("sound")).forGetter(ExplodeEnchantmentEffect::sound)).apply((Applicative<ExplodeEnchantmentEffect, ?>)instance, ExplodeEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        Vec3d vec3d = pos.add(this.offset);
        world.createExplosion(this.attributeToUser ? user : null, this.getDamageSource(user, vec3d), new AdvancedExplosionBehavior(this.blockInteraction != World.ExplosionSourceType.NONE, this.damageType.isPresent(), this.knockbackMultiplier.map(knockbackMultiplier -> Float.valueOf(knockbackMultiplier.getValue(level))), this.immuneBlocks), vec3d.getX(), vec3d.getY(), vec3d.getZ(), Math.max(this.radius.getValue(level), 0.0f), this.createFire, this.blockInteraction, this.smallParticle, this.largeParticle, this.sound);
    }

    @Nullable
    private DamageSource getDamageSource(Entity user, Vec3d pos) {
        if (this.damageType.isEmpty()) {
            return null;
        }
        if (this.attributeToUser) {
            return new DamageSource(this.damageType.get(), user);
        }
        return new DamageSource(this.damageType.get(), pos);
    }

    public MapCodec<ExplodeEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

