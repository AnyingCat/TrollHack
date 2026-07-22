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
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.Nameable;
import net.minecraft.util.StringIdentifiable;

public class CopyNameLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<CopyNameLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> CopyNameLootFunction.addConditionsField(instance).and(((MapCodec)Source.CODEC.fieldOf("source")).forGetter(function -> function.source)).apply((Applicative<CopyNameLootFunction, ?>)instance, CopyNameLootFunction::new));
    private final Source source;

    private CopyNameLootFunction(List<LootCondition> conditions, Source source) {
        super(conditions);
        this.source = source;
    }

    public LootFunctionType<CopyNameLootFunction> getType() {
        return LootFunctionTypes.COPY_NAME;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(this.source.parameter);
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        Object object = context.get(this.source.parameter);
        if (object instanceof Nameable) {
            Nameable nameable = (Nameable)object;
            stack.set(DataComponentTypes.CUSTOM_NAME, nameable.getCustomName());
        }
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(Source source) {
        return CopyNameLootFunction.builder((List<LootCondition> conditions) -> new CopyNameLootFunction((List<LootCondition>)conditions, source));
    }

    public static enum Source implements StringIdentifiable
    {
        THIS("this", LootContextParameters.THIS_ENTITY),
        ATTACKING_ENTITY("attacking_entity", LootContextParameters.ATTACKING_ENTITY),
        LAST_DAMAGE_PLAYER("last_damage_player", LootContextParameters.LAST_DAMAGE_PLAYER),
        BLOCK_ENTITY("block_entity", LootContextParameters.BLOCK_ENTITY);

        public static final Codec<Source> CODEC;
        private final String name;
        final LootContextParameter<?> parameter;

        private Source(String name, LootContextParameter<?> parameter) {
            this.name = name;
            this.parameter = parameter;
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Source::values);
        }
    }
}

