/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;

public record EnchantmentActiveCheckLootCondition(boolean active) implements LootCondition
{
    public static final MapCodec<EnchantmentActiveCheckLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.BOOL.fieldOf("active")).forGetter(EnchantmentActiveCheckLootCondition::active)).apply((Applicative<EnchantmentActiveCheckLootCondition, ?>)instance, EnchantmentActiveCheckLootCondition::new));

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.requireParameter(LootContextParameters.ENCHANTMENT_ACTIVE) == this.active;
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.ENCHANTMENT_ACTIVE_CHECK;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Set.of(LootContextParameters.ENCHANTMENT_ACTIVE);
    }

    public static LootCondition.Builder requireActive() {
        return () -> new EnchantmentActiveCheckLootCondition(true);
    }

    public static LootCondition.Builder requireInactive() {
        return () -> new EnchantmentActiveCheckLootCondition(false);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

