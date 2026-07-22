/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public interface AllOfEnchantmentEffects {
    public static <T, A extends T> MapCodec<A> buildCodec(Codec<T> baseCodec, Function<List<T>, A> fromList, Function<A, List<T>> toList) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)baseCodec.listOf().fieldOf("effects")).forGetter(toList)).apply(instance, fromList));
    }

    public static EntityEffects allOf(EnchantmentEntityEffect ... entityEffects) {
        return new EntityEffects(List.of(entityEffects));
    }

    public static LocationBasedEffects allOf(EnchantmentLocationBasedEffect ... locationBasedEffects) {
        return new LocationBasedEffects(List.of(locationBasedEffects));
    }

    public static ValueEffects allOf(EnchantmentValueEffect ... valueEffects) {
        return new ValueEffects(List.of(valueEffects));
    }

    public record EntityEffects(List<EnchantmentEntityEffect> effects) implements EnchantmentEntityEffect
    {
        public static final MapCodec<EntityEffects> CODEC = AllOfEnchantmentEffects.buildCodec(EnchantmentEntityEffect.CODEC, EntityEffects::new, EntityEffects::effects);

        @Override
        public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
            for (EnchantmentEntityEffect enchantmentEntityEffect : this.effects) {
                enchantmentEntityEffect.apply(world, level, context, user, pos);
            }
        }

        public MapCodec<EntityEffects> getCodec() {
            return CODEC;
        }
    }

    public record LocationBasedEffects(List<EnchantmentLocationBasedEffect> effects) implements EnchantmentLocationBasedEffect
    {
        public static final MapCodec<LocationBasedEffects> CODEC = AllOfEnchantmentEffects.buildCodec(EnchantmentLocationBasedEffect.CODEC, LocationBasedEffects::new, LocationBasedEffects::effects);

        @Override
        public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos, boolean newlyApplied) {
            for (EnchantmentLocationBasedEffect enchantmentLocationBasedEffect : this.effects) {
                enchantmentLocationBasedEffect.apply(world, level, context, user, pos, newlyApplied);
            }
        }

        @Override
        public void remove(EnchantmentEffectContext context, Entity user, Vec3d pos, int level) {
            for (EnchantmentLocationBasedEffect enchantmentLocationBasedEffect : this.effects) {
                enchantmentLocationBasedEffect.remove(context, user, pos, level);
            }
        }

        public MapCodec<LocationBasedEffects> getCodec() {
            return CODEC;
        }
    }

    public record ValueEffects(List<EnchantmentValueEffect> effects) implements EnchantmentValueEffect
    {
        public static final MapCodec<ValueEffects> CODEC = AllOfEnchantmentEffects.buildCodec(EnchantmentValueEffect.CODEC, ValueEffects::new, ValueEffects::effects);

        @Override
        public float apply(int level, Random random, float inputValue) {
            for (EnchantmentValueEffect enchantmentValueEffect : this.effects) {
                inputValue = enchantmentValueEffect.apply(level, random, inputValue);
            }
            return inputValue;
        }

        public MapCodec<ValueEffects> getCodec() {
            return CODEC;
        }
    }
}

