/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.random.Random;

public record SpawnParticlesEnchantmentEffect(ParticleEffect particle, PositionSource horizontalPosition, PositionSource verticalPosition, VelocitySource horizontalVelocity, VelocitySource verticalVelocity, FloatProvider speed) implements EnchantmentEntityEffect
{
    public static final MapCodec<SpawnParticlesEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ParticleTypes.TYPE_CODEC.fieldOf("particle")).forGetter(SpawnParticlesEnchantmentEffect::particle), PositionSource.CODEC.fieldOf("horizontal_position").forGetter(SpawnParticlesEnchantmentEffect::horizontalPosition), PositionSource.CODEC.fieldOf("vertical_position").forGetter(SpawnParticlesEnchantmentEffect::verticalPosition), VelocitySource.CODEC.fieldOf("horizontal_velocity").forGetter(SpawnParticlesEnchantmentEffect::horizontalVelocity), VelocitySource.CODEC.fieldOf("vertical_velocity").forGetter(SpawnParticlesEnchantmentEffect::verticalVelocity), FloatProvider.VALUE_CODEC.optionalFieldOf("speed", ConstantFloatProvider.ZERO).forGetter(SpawnParticlesEnchantmentEffect::speed)).apply((Applicative<SpawnParticlesEnchantmentEffect, ?>)instance, SpawnParticlesEnchantmentEffect::new));

    public static PositionSource entityPosition(float offset) {
        return new PositionSource(PositionSourceType.ENTITY_POSITION, offset, 1.0f);
    }

    public static PositionSource withinBoundingBox() {
        return new PositionSource(PositionSourceType.BOUNDING_BOX, 0.0f, 1.0f);
    }

    public static VelocitySource scaledVelocity(float movementScale) {
        return new VelocitySource(movementScale, ConstantFloatProvider.ZERO);
    }

    public static VelocitySource fixedVelocity(FloatProvider base) {
        return new VelocitySource(0.0f, base);
    }

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        Random random = user.getRandom();
        Vec3d vec3d = user.getMovement();
        float f = user.getWidth();
        float g = user.getHeight();
        world.spawnParticles(this.particle, this.horizontalPosition.getPosition(pos.getX(), pos.getX(), f, random), this.verticalPosition.getPosition(pos.getY(), pos.getY() + (double)(g / 2.0f), g, random), this.horizontalPosition.getPosition(pos.getZ(), pos.getZ(), f, random), 0, this.horizontalVelocity.getVelocity(vec3d.getX(), random), this.verticalVelocity.getVelocity(vec3d.getY(), random), this.horizontalVelocity.getVelocity(vec3d.getZ(), random), this.speed.get(random));
    }

    public MapCodec<SpawnParticlesEnchantmentEffect> getCodec() {
        return CODEC;
    }

    public record PositionSource(PositionSourceType type, float offset, float scale) {
        public static final MapCodec<PositionSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)PositionSourceType.CODEC.fieldOf("type")).forGetter(PositionSource::type), Codec.FLOAT.optionalFieldOf("offset", Float.valueOf(0.0f)).forGetter(PositionSource::offset), Codecs.POSITIVE_FLOAT.optionalFieldOf("scale", Float.valueOf(1.0f)).forGetter(PositionSource::scale)).apply((Applicative<PositionSource, ?>)instance, PositionSource::new)).validate(source -> {
            if (source.type() == PositionSourceType.ENTITY_POSITION && source.scale() != 1.0f) {
                return DataResult.error(() -> "Cannot scale an entity position coordinate source");
            }
            return DataResult.success(source);
        });

        public double getPosition(double entityPosition, double boundingBoxCenter, float boundingBoxSize, Random random) {
            return this.type.getCoordinate(entityPosition, boundingBoxCenter, boundingBoxSize * this.scale, random) + (double)this.offset;
        }
    }

    public record VelocitySource(float movementScale, FloatProvider base) {
        public static final MapCodec<VelocitySource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.FLOAT.optionalFieldOf("movement_scale", Float.valueOf(0.0f)).forGetter(VelocitySource::movementScale), FloatProvider.VALUE_CODEC.optionalFieldOf("base", ConstantFloatProvider.ZERO).forGetter(VelocitySource::base)).apply((Applicative<VelocitySource, ?>)instance, VelocitySource::new));

        public double getVelocity(double entityVelocity, Random random) {
            return entityVelocity * (double)this.movementScale + (double)this.base.get(random);
        }
    }

    public static enum PositionSourceType implements StringIdentifiable
    {
        ENTITY_POSITION("entity_position", (entityPosition, boundingBoxCenter, boundingBoxSize, random) -> entityPosition),
        BOUNDING_BOX("in_bounding_box", (entityPosition, boundingBoxCenter, boundingBoxSize, random) -> boundingBoxCenter + (random.nextDouble() - 0.5) * (double)boundingBoxSize);

        public static final Codec<PositionSourceType> CODEC;
        private final String id;
        private final CoordinateSource coordinateSource;

        private PositionSourceType(String id, CoordinateSource coordinateSource) {
            this.id = id;
            this.coordinateSource = coordinateSource;
        }

        public double getCoordinate(double entityPosition, double boundingBoxCenter, float boundingBoxSize, Random random) {
            return this.coordinateSource.getCoordinate(entityPosition, boundingBoxCenter, boundingBoxSize, random);
        }

        @Override
        public String asString() {
            return this.id;
        }

        static {
            CODEC = StringIdentifiable.createCodec(PositionSourceType::values);
        }

        @FunctionalInterface
        static interface CoordinateSource {
            public double getCoordinate(double var1, double var3, float var5, Random var6);
        }
    }
}

