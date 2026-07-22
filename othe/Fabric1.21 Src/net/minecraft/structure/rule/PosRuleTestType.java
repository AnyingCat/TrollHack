/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.rule;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.rule.AlwaysTruePosRuleTest;
import net.minecraft.structure.rule.AxisAlignedLinearPosRuleTest;
import net.minecraft.structure.rule.LinearPosRuleTest;
import net.minecraft.structure.rule.PosRuleTest;

public interface PosRuleTestType<P extends PosRuleTest> {
    public static final PosRuleTestType<AlwaysTruePosRuleTest> ALWAYS_TRUE = PosRuleTestType.register("always_true", AlwaysTruePosRuleTest.CODEC);
    public static final PosRuleTestType<LinearPosRuleTest> LINEAR_POS = PosRuleTestType.register("linear_pos", LinearPosRuleTest.CODEC);
    public static final PosRuleTestType<AxisAlignedLinearPosRuleTest> AXIS_ALIGNED_LINEAR_POS = PosRuleTestType.register("axis_aligned_linear_pos", AxisAlignedLinearPosRuleTest.CODEC);

    public MapCodec<P> codec();

    public static <P extends PosRuleTest> PosRuleTestType<P> register(String id, MapCodec<P> codec) {
        return Registry.register(Registries.POS_RULE_TEST, id, () -> codec);
    }
}

