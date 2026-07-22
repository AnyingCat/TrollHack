/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Codecs;

public record TableBonusLootCondition(RegistryEntry<Enchantment> enchantment, List<Float> chances) implements LootCondition
{
    public static final MapCodec<TableBonusLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Enchantment.ENTRY_CODEC.fieldOf("enchantment")).forGetter(TableBonusLootCondition::enchantment), ((MapCodec)Codecs.nonEmptyList(Codec.FLOAT.listOf()).fieldOf("chances")).forGetter(TableBonusLootCondition::chances)).apply((Applicative<TableBonusLootCondition, ?>)instance, TableBonusLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.TABLE_BONUS;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.TOOL);
    }

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack itemStack = lootContext.get(LootContextParameters.TOOL);
        int i = itemStack != null ? EnchantmentHelper.getLevel(this.enchantment, itemStack) : 0;
        float f = this.chances.get(Math.min(i, this.chances.size() - 1)).floatValue();
        return lootContext.getRandom().nextFloat() < f;
    }

    public static LootCondition.Builder builder(RegistryEntry<Enchantment> enchantment, float ... chances) {
        ArrayList<Float> list = new ArrayList<Float>(chances.length);
        for (float f : chances) {
            list.add(Float.valueOf(f));
        }
        return () -> new TableBonusLootCondition(enchantment, list);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

