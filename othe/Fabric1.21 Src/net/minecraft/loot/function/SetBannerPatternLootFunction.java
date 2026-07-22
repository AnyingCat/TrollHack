/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;

public class SetBannerPatternLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetBannerPatternLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetBannerPatternLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)BannerPatternsComponent.CODEC.fieldOf("patterns")).forGetter(function -> function.patterns), ((MapCodec)Codec.BOOL.fieldOf("append")).forGetter(function -> function.append))).apply((Applicative<SetBannerPatternLootFunction, ?>)instance, SetBannerPatternLootFunction::new));
    private final BannerPatternsComponent patterns;
    private final boolean append;

    SetBannerPatternLootFunction(List<LootCondition> conditions, BannerPatternsComponent patterns, boolean append) {
        super(conditions);
        this.patterns = patterns;
        this.append = append;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        if (this.append) {
            stack.apply(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT, this.patterns, (current, newPatterns) -> new BannerPatternsComponent.Builder().addAll((BannerPatternsComponent)current).addAll((BannerPatternsComponent)newPatterns).build());
        } else {
            stack.set(DataComponentTypes.BANNER_PATTERNS, this.patterns);
        }
        return stack;
    }

    public LootFunctionType<SetBannerPatternLootFunction> getType() {
        return LootFunctionTypes.SET_BANNER_PATTERN;
    }

    public static Builder builder(boolean append) {
        return new Builder(append);
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final BannerPatternsComponent.Builder patterns = new BannerPatternsComponent.Builder();
        private final boolean append;

        Builder(boolean append) {
            this.append = append;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetBannerPatternLootFunction(this.getConditions(), this.patterns.build(), this.append);
        }

        public Builder pattern(RegistryEntry<BannerPattern> pattern, DyeColor color) {
            this.patterns.add(pattern, color);
            return this;
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

