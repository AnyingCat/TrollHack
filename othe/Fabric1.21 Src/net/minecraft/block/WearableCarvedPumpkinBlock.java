/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;

public class WearableCarvedPumpkinBlock
extends CarvedPumpkinBlock
implements Equipment {
    public static final MapCodec<WearableCarvedPumpkinBlock> CODEC = WearableCarvedPumpkinBlock.createCodec(WearableCarvedPumpkinBlock::new);

    public MapCodec<WearableCarvedPumpkinBlock> getCodec() {
        return CODEC;
    }

    public WearableCarvedPumpkinBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }
}

