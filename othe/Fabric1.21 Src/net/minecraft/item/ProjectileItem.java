/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.OptionalInt;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ProjectileItem {
    public ProjectileEntity createEntity(World var1, Position var2, ItemStack var3, Direction var4);

    default public Settings getProjectileSettings() {
        return Settings.DEFAULT;
    }

    default public void initializeProjectile(ProjectileEntity entity, double x, double y, double z, float power, float uncertainty) {
        entity.setVelocity(x, y, z, power, uncertainty);
    }

    public record Settings(PositionFunction positionFunction, float uncertainty, float power, OptionalInt overrideDispenseEvent) {
        public static final Settings DEFAULT = Settings.builder().build();

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private PositionFunction positionFunction = (pointer, direction) -> DispenserBlock.getOutputLocation(pointer, 0.7, new Vec3d(0.0, 0.1, 0.0));
            private float uncertainty = 6.0f;
            private float power = 1.1f;
            private OptionalInt overrideDispenserEvent = OptionalInt.empty();

            public Builder positionFunction(PositionFunction positionFunction) {
                this.positionFunction = positionFunction;
                return this;
            }

            public Builder uncertainty(float uncertainty) {
                this.uncertainty = uncertainty;
                return this;
            }

            public Builder power(float power) {
                this.power = power;
                return this;
            }

            public Builder overrideDispenseEvent(int overrideDispenseEvent) {
                this.overrideDispenserEvent = OptionalInt.of(overrideDispenseEvent);
                return this;
            }

            public Settings build() {
                return new Settings(this.positionFunction, this.uncertainty, this.power, this.overrideDispenserEvent);
            }
        }
    }

    @FunctionalInterface
    public static interface PositionFunction {
        public Position getDispensePosition(BlockPointer var1, Direction var2);
    }
}

