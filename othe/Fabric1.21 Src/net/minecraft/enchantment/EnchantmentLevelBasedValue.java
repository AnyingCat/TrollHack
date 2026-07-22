/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.MathHelper;

public interface EnchantmentLevelBasedValue {
    public static final Codec<EnchantmentLevelBasedValue> BASE_CODEC = Registries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE.getCodec().dispatch(EnchantmentLevelBasedValue::getCodec, codec -> codec);
    public static final Codec<EnchantmentLevelBasedValue> CODEC = Codec.either(Constant.CODEC, BASE_CODEC).xmap(either -> either.map(type -> type, type -> type), type -> {
        Either<Object, EnchantmentLevelBasedValue> either;
        if (type instanceof Constant) {
            Constant constant = (Constant)type;
            either = Either.left(constant);
        } else {
            either = Either.right(type);
        }
        return either;
    });

    public static MapCodec<? extends EnchantmentLevelBasedValue> registerAndGetDefault(Registry<MapCodec<? extends EnchantmentLevelBasedValue>> registry) {
        Registry.register(registry, "clamped", Clamped.CODEC);
        Registry.register(registry, "fraction", Fraction.CODEC);
        Registry.register(registry, "levels_squared", LevelsSquared.CODEC);
        Registry.register(registry, "linear", Linear.CODEC);
        return Registry.register(registry, "lookup", Lookup.CODEC);
    }

    public static Constant constant(float value) {
        return new Constant(value);
    }

    public static Linear linear(float base, float perLevelAboveFirst) {
        return new Linear(base, perLevelAboveFirst);
    }

    public static Linear linear(float base) {
        return EnchantmentLevelBasedValue.linear(base, base);
    }

    public static Lookup lookup(List<Float> values, EnchantmentLevelBasedValue fallback) {
        return new Lookup(values, fallback);
    }

    public float getValue(int var1);

    public MapCodec<? extends EnchantmentLevelBasedValue> getCodec();

    public record Clamped(EnchantmentLevelBasedValue value, float min, float max) implements EnchantmentLevelBasedValue
    {
        public static final MapCodec<Clamped> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)CODEC.fieldOf("value")).forGetter(Clamped::value), ((MapCodec)Codec.FLOAT.fieldOf("min")).forGetter(Clamped::min), ((MapCodec)Codec.FLOAT.fieldOf("max")).forGetter(Clamped::max)).apply((Applicative<Clamped, ?>)instance, Clamped::new)).validate(type -> {
            if (type.max <= type.min) {
                return DataResult.error(() -> "Max must be larger than min, min: " + clamped.min + ", max: " + clamped.max);
            }
            return DataResult.success(type);
        });

        @Override
        public float getValue(int level) {
            return MathHelper.clamp(this.value.getValue(level), this.min, this.max);
        }

        public MapCodec<Clamped> getCodec() {
            return CODEC;
        }
    }

    public record Fraction(EnchantmentLevelBasedValue numerator, EnchantmentLevelBasedValue denominator) implements EnchantmentLevelBasedValue
    {
        public static final MapCodec<Fraction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)CODEC.fieldOf("numerator")).forGetter(Fraction::numerator), ((MapCodec)CODEC.fieldOf("denominator")).forGetter(Fraction::denominator)).apply((Applicative<Fraction, ?>)instance, Fraction::new));

        @Override
        public float getValue(int level) {
            float f = this.denominator.getValue(level);
            if (f == 0.0f) {
                return 0.0f;
            }
            return this.numerator.getValue(level) / f;
        }

        public MapCodec<Fraction> getCodec() {
            return CODEC;
        }
    }

    public record LevelsSquared(float added) implements EnchantmentLevelBasedValue
    {
        public static final MapCodec<LevelsSquared> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("added")).forGetter(LevelsSquared::added)).apply((Applicative<LevelsSquared, ?>)instance, LevelsSquared::new));

        @Override
        public float getValue(int level) {
            return (float)MathHelper.square(level) + this.added;
        }

        public MapCodec<LevelsSquared> getCodec() {
            return CODEC;
        }
    }

    public record Linear(float base, float perLevelAboveFirst) implements EnchantmentLevelBasedValue
    {
        public static final MapCodec<Linear> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("base")).forGetter(Linear::base), ((MapCodec)Codec.FLOAT.fieldOf("per_level_above_first")).forGetter(Linear::perLevelAboveFirst)).apply((Applicative<Linear, ?>)instance, Linear::new));

        @Override
        public float getValue(int level) {
            return this.base + this.perLevelAboveFirst * (float)(level - 1);
        }

        public MapCodec<Linear> getCodec() {
            return CODEC;
        }
    }

    public record Lookup(List<Float> values, EnchantmentLevelBasedValue fallback) implements EnchantmentLevelBasedValue
    {
        public static final MapCodec<Lookup> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.listOf().fieldOf("values")).forGetter(Lookup::values), ((MapCodec)CODEC.fieldOf("fallback")).forGetter(Lookup::fallback)).apply((Applicative<Lookup, ?>)instance, Lookup::new));

        @Override
        public float getValue(int level) {
            return level <= this.values.size() ? this.values.get(level - 1).floatValue() : this.fallback.getValue(level);
        }

        public MapCodec<Lookup> getCodec() {
            return CODEC;
        }
    }

    public record Constant(float value) implements EnchantmentLevelBasedValue
    {
        public static final Codec<Constant> CODEC = Codec.FLOAT.xmap(Constant::new, Constant::value);
        public static final MapCodec<Constant> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("value")).forGetter(Constant::value)).apply((Applicative<Constant, ?>)instance, Constant::new));

        @Override
        public float getValue(int level) {
            return this.value;
        }

        public MapCodec<Constant> getCodec() {
            return TYPE_CODEC;
        }
    }
}

