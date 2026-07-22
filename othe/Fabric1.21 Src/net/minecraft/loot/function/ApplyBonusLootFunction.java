/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;

public class ApplyBonusLootFunction
extends ConditionalLootFunction {
    private static final Map<Identifier, Type> FACTORIES = Stream.of(BinomialWithBonusCount.TYPE, OreDrops.TYPE, UniformBonusCount.TYPE).collect(Collectors.toMap(Type::id, Function.identity()));
    private static final Codec<Type> TYPE_CODEC = Identifier.CODEC.comapFlatMap(id -> {
        Type type = FACTORIES.get(id);
        if (type != null) {
            return DataResult.success(type);
        }
        return DataResult.error(() -> "No formula type with id: '" + String.valueOf(id) + "'");
    }, Type::id);
    private static final MapCodec<Formula> FORMULA_CODEC = Codecs.parameters("formula", "parameters", TYPE_CODEC, Formula::getType, Type::codec);
    public static final MapCodec<ApplyBonusLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> ApplyBonusLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)Enchantment.ENTRY_CODEC.fieldOf("enchantment")).forGetter(function -> function.enchantment), FORMULA_CODEC.forGetter(function -> function.formula))).apply((Applicative<ApplyBonusLootFunction, ?>)instance, ApplyBonusLootFunction::new));
    private final RegistryEntry<Enchantment> enchantment;
    private final Formula formula;

    private ApplyBonusLootFunction(List<LootCondition> conditions, RegistryEntry<Enchantment> enchantment, Formula formula) {
        super(conditions);
        this.enchantment = enchantment;
        this.formula = formula;
    }

    public LootFunctionType<ApplyBonusLootFunction> getType() {
        return LootFunctionTypes.APPLY_BONUS;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.TOOL);
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        ItemStack itemStack = context.get(LootContextParameters.TOOL);
        if (itemStack != null) {
            int i = EnchantmentHelper.getLevel(this.enchantment, itemStack);
            int j = this.formula.getValue(context.getRandom(), stack.getCount(), i);
            stack.setCount(j);
        }
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> binomialWithBonusCount(RegistryEntry<Enchantment> enchantment, float probability, int extra) {
        return ApplyBonusLootFunction.builder(conditions -> new ApplyBonusLootFunction((List<LootCondition>)conditions, enchantment, new BinomialWithBonusCount(extra, probability)));
    }

    public static ConditionalLootFunction.Builder<?> oreDrops(RegistryEntry<Enchantment> enchantment) {
        return ApplyBonusLootFunction.builder(conditions -> new ApplyBonusLootFunction((List<LootCondition>)conditions, enchantment, new OreDrops()));
    }

    public static ConditionalLootFunction.Builder<?> uniformBonusCount(RegistryEntry<Enchantment> enchantment) {
        return ApplyBonusLootFunction.builder(conditions -> new ApplyBonusLootFunction((List<LootCondition>)conditions, enchantment, new UniformBonusCount(1)));
    }

    public static ConditionalLootFunction.Builder<?> uniformBonusCount(RegistryEntry<Enchantment> enchantment, int bonusMultiplier) {
        return ApplyBonusLootFunction.builder(conditions -> new ApplyBonusLootFunction((List<LootCondition>)conditions, enchantment, new UniformBonusCount(bonusMultiplier)));
    }

    static interface Formula {
        public int getValue(Random var1, int var2, int var3);

        public Type getType();
    }

    record UniformBonusCount(int bonusMultiplier) implements Formula
    {
        public static final Codec<UniformBonusCount> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("bonusMultiplier")).forGetter(UniformBonusCount::bonusMultiplier)).apply((Applicative<UniformBonusCount, ?>)instance, UniformBonusCount::new));
        public static final Type TYPE = new Type(Identifier.ofVanilla("uniform_bonus_count"), CODEC);

        @Override
        public int getValue(Random random, int initialCount, int enchantmentLevel) {
            return initialCount + random.nextInt(this.bonusMultiplier * enchantmentLevel + 1);
        }

        @Override
        public Type getType() {
            return TYPE;
        }
    }

    record OreDrops() implements Formula
    {
        public static final Codec<OreDrops> CODEC = Codec.unit(OreDrops::new);
        public static final Type TYPE = new Type(Identifier.ofVanilla("ore_drops"), CODEC);

        @Override
        public int getValue(Random random, int initialCount, int enchantmentLevel) {
            if (enchantmentLevel > 0) {
                int i = random.nextInt(enchantmentLevel + 2) - 1;
                if (i < 0) {
                    i = 0;
                }
                return initialCount * (i + 1);
            }
            return initialCount;
        }

        @Override
        public Type getType() {
            return TYPE;
        }
    }

    record BinomialWithBonusCount(int extra, float probability) implements Formula
    {
        private static final Codec<BinomialWithBonusCount> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("extra")).forGetter(BinomialWithBonusCount::extra), ((MapCodec)Codec.FLOAT.fieldOf("probability")).forGetter(BinomialWithBonusCount::probability)).apply((Applicative<BinomialWithBonusCount, ?>)instance, BinomialWithBonusCount::new));
        public static final Type TYPE = new Type(Identifier.ofVanilla("binomial_with_bonus_count"), CODEC);

        @Override
        public int getValue(Random random, int initialCount, int enchantmentLevel) {
            for (int i = 0; i < enchantmentLevel + this.extra; ++i) {
                if (!(random.nextFloat() < this.probability)) continue;
                ++initialCount;
            }
            return initialCount;
        }

        @Override
        public Type getType() {
            return TYPE;
        }
    }

    record Type(Identifier id, Codec<? extends Formula> codec) {
    }
}

