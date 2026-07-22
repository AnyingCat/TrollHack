/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;

public class CopyComponentsLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<CopyComponentsLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> CopyComponentsLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)Source.CODEC.fieldOf("source")).forGetter(function -> function.source), ComponentType.CODEC.listOf().optionalFieldOf("include").forGetter(function -> function.include), ComponentType.CODEC.listOf().optionalFieldOf("exclude").forGetter(function -> function.exclude))).apply((Applicative<CopyComponentsLootFunction, ?>)instance, CopyComponentsLootFunction::new));
    private final Source source;
    private final Optional<List<ComponentType<?>>> include;
    private final Optional<List<ComponentType<?>>> exclude;
    private final Predicate<ComponentType<?>> filter;

    CopyComponentsLootFunction(List<LootCondition> conditions, Source source, Optional<List<ComponentType<?>>> include, Optional<List<ComponentType<?>>> exclude) {
        super(conditions);
        this.source = source;
        this.include = include.map(List::copyOf);
        this.exclude = exclude.map(List::copyOf);
        ArrayList list = new ArrayList(2);
        exclude.ifPresent(excludedTypes -> list.add(type -> !excludedTypes.contains(type)));
        include.ifPresent(includedTypes -> list.add(includedTypes::contains));
        this.filter = Util.allOf(list);
    }

    public LootFunctionType<CopyComponentsLootFunction> getType() {
        return LootFunctionTypes.COPY_COMPONENTS;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.source.getRequiredParameters();
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        ComponentMap componentMap = this.source.getComponents(context);
        stack.applyComponentsFrom(componentMap.filtered(this.filter));
        return stack;
    }

    public static Builder builder(Source source) {
        return new Builder(source);
    }

    public static enum Source implements StringIdentifiable
    {
        BLOCK_ENTITY("block_entity");

        public static final Codec<Source> CODEC;
        private final String id;

        private Source(String id) {
            this.id = id;
        }

        public ComponentMap getComponents(LootContext context) {
            switch (this.ordinal()) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: 
            }
            BlockEntity blockEntity = context.get(LootContextParameters.BLOCK_ENTITY);
            return blockEntity != null ? blockEntity.createComponentMap() : ComponentMap.EMPTY;
        }

        public Set<LootContextParameter<?>> getRequiredParameters() {
            switch (this.ordinal()) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: 
            }
            return Set.of(LootContextParameters.BLOCK_ENTITY);
        }

        @Override
        public String asString() {
            return this.id;
        }

        static {
            CODEC = StringIdentifiable.createBasicCodec(Source::values);
        }
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final Source source;
        private Optional<ImmutableList.Builder<ComponentType<?>>> include = Optional.empty();
        private Optional<ImmutableList.Builder<ComponentType<?>>> exclude = Optional.empty();

        Builder(Source source) {
            this.source = source;
        }

        public Builder include(ComponentType<?> type) {
            if (this.include.isEmpty()) {
                this.include = Optional.of(ImmutableList.builder());
            }
            this.include.get().add((Object)type);
            return this;
        }

        public Builder exclude(ComponentType<?> type) {
            if (this.exclude.isEmpty()) {
                this.exclude = Optional.of(ImmutableList.builder());
            }
            this.exclude.get().add((Object)type);
            return this;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new CopyComponentsLootFunction(this.getConditions(), this.source, this.include.map(ImmutableList.Builder::build), this.exclude.map(ImmutableList.Builder::build));
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

