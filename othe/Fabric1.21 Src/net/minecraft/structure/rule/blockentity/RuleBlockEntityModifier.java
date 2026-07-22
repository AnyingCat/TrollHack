/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.rule.blockentity;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.structure.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public interface RuleBlockEntityModifier {
    public static final Codec<RuleBlockEntityModifier> TYPE_CODEC = Registries.RULE_BLOCK_ENTITY_MODIFIER.getCodec().dispatch(RuleBlockEntityModifier::getType, RuleBlockEntityModifierType::codec);

    @Nullable
    public NbtCompound modifyBlockEntityNbt(Random var1, @Nullable NbtCompound var2);

    public RuleBlockEntityModifierType<?> getType();
}

