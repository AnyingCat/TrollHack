/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.property.Property;

public class CopyStateLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<CopyStateLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> CopyStateLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)Registries.BLOCK.getEntryCodec().fieldOf("block")).forGetter(function -> function.block), ((MapCodec)Codec.STRING.listOf().fieldOf("properties")).forGetter(function -> function.properties.stream().map(Property::getName).toList()))).apply((Applicative<CopyStateLootFunction, ?>)instance, CopyStateLootFunction::new));
    private final RegistryEntry<Block> block;
    private final Set<Property<?>> properties;

    CopyStateLootFunction(List<LootCondition> conditions, RegistryEntry<Block> block, Set<Property<?>> properties) {
        super(conditions);
        this.block = block;
        this.properties = properties;
    }

    private CopyStateLootFunction(List<LootCondition> conditions, RegistryEntry<Block> block, List<String> properties) {
        this(conditions, block, properties.stream().map(block.value().getStateManager()::getProperty).filter(Objects::nonNull).collect(Collectors.toSet()));
    }

    public LootFunctionType<CopyStateLootFunction> getType() {
        return LootFunctionTypes.COPY_STATE;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.BLOCK_STATE);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        BlockState blockState = context.get(LootContextParameters.BLOCK_STATE);
        if (blockState != null) {
            stack.apply(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT, component -> {
                for (Property<?> property : this.properties) {
                    if (!blockState.contains(property)) continue;
                    component = component.with(property, blockState);
                }
                return component;
            });
        }
        return stack;
    }

    public static Builder builder(Block block) {
        return new Builder(block);
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final RegistryEntry<Block> block;
        private final ImmutableSet.Builder<Property<?>> properties = ImmutableSet.builder();

        Builder(Block block) {
            this.block = block.getRegistryEntry();
        }

        public Builder addProperty(Property<?> property) {
            if (!this.block.value().getStateManager().getProperties().contains(property)) {
                throw new IllegalStateException("Property " + String.valueOf(property) + " is not present on block " + String.valueOf(this.block));
            }
            this.properties.add((Object)property);
            return this;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new CopyStateLootFunction(this.getConditions(), this.block, (Set<Property<?>>)((Object)this.properties.build()));
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

