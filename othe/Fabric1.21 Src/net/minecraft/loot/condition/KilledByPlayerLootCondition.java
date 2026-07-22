/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;

public class KilledByPlayerLootCondition
implements LootCondition {
    private static final KilledByPlayerLootCondition INSTANCE = new KilledByPlayerLootCondition();
    public static final MapCodec<KilledByPlayerLootCondition> CODEC = MapCodec.unit(INSTANCE);

    private KilledByPlayerLootCondition() {
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.KILLED_BY_PLAYER;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.LAST_DAMAGE_PLAYER);
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.hasParameter(LootContextParameters.LAST_DAMAGE_PLAYER);
    }

    public static LootCondition.Builder builder() {
        return () -> INSTANCE;
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

