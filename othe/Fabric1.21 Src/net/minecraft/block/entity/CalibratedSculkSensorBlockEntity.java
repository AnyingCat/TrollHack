/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.CalibratedSculkSensorBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.Vibrations;
import org.jetbrains.annotations.Nullable;

public class CalibratedSculkSensorBlockEntity
extends SculkSensorBlockEntity {
    public CalibratedSculkSensorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.CALIBRATED_SCULK_SENSOR, blockPos, blockState);
    }

    @Override
    public Vibrations.Callback createCallback() {
        return new Callback(this.getPos());
    }

    protected class Callback
    extends SculkSensorBlockEntity.VibrationCallback {
        public Callback(BlockPos pos) {
            super(CalibratedSculkSensorBlockEntity.this, pos);
        }

        @Override
        public int getRange() {
            return 16;
        }

        @Override
        public boolean accepts(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, @Nullable GameEvent.Emitter emitter) {
            int i = this.getCalibrationFrequency(world, this.pos, CalibratedSculkSensorBlockEntity.this.getCachedState());
            if (i != 0 && Vibrations.getFrequency(event) != i) {
                return false;
            }
            return super.accepts(world, pos, event, emitter);
        }

        private int getCalibrationFrequency(World world, BlockPos pos, BlockState state) {
            Direction direction = state.get(CalibratedSculkSensorBlock.FACING).getOpposite();
            return world.getEmittedRedstonePower(pos.offset(direction), direction);
        }
    }
}

