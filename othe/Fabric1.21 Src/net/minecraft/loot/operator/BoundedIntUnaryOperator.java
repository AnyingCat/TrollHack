/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.operator;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class BoundedIntUnaryOperator {
    private static final Codec<BoundedIntUnaryOperator> OPERATOR_CODEC = RecordCodecBuilder.create(instance -> instance.group(LootNumberProviderTypes.CODEC.optionalFieldOf("min").forGetter(operator -> Optional.ofNullable(operator.min)), LootNumberProviderTypes.CODEC.optionalFieldOf("max").forGetter(operator -> Optional.ofNullable(operator.max))).apply((Applicative<BoundedIntUnaryOperator, ?>)instance, BoundedIntUnaryOperator::new));
    public static final Codec<BoundedIntUnaryOperator> CODEC = Codec.either(Codec.INT, OPERATOR_CODEC).xmap(either -> either.map(BoundedIntUnaryOperator::create, Function.identity()), operator -> {
        OptionalInt optionalInt = operator.getConstantValue();
        if (optionalInt.isPresent()) {
            return Either.left(optionalInt.getAsInt());
        }
        return Either.right(operator);
    });
    @Nullable
    private final LootNumberProvider min;
    @Nullable
    private final LootNumberProvider max;
    private final Applier applier;
    private final Tester tester;

    public Set<LootContextParameter<?>> getRequiredParameters() {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        if (this.min != null) {
            builder.addAll(this.min.getRequiredParameters());
        }
        if (this.max != null) {
            builder.addAll(this.max.getRequiredParameters());
        }
        return builder.build();
    }

    private BoundedIntUnaryOperator(Optional<LootNumberProvider> min, Optional<LootNumberProvider> max) {
        this((LootNumberProvider)min.orElse(null), (LootNumberProvider)max.orElse(null));
    }

    private BoundedIntUnaryOperator(@Nullable LootNumberProvider min, @Nullable LootNumberProvider max) {
        this.min = min;
        this.max = max;
        if (min == null) {
            if (max == null) {
                this.applier = (context, value) -> value;
                this.tester = (context, value) -> true;
            } else {
                this.applier = (context, value) -> Math.min(max.nextInt(context), value);
                this.tester = (context, value) -> value <= max.nextInt(context);
            }
        } else if (max == null) {
            this.applier = (context, value) -> Math.max(min.nextInt(context), value);
            this.tester = (context, value) -> value >= min.nextInt(context);
        } else {
            this.applier = (context, value) -> MathHelper.clamp(value, min.nextInt(context), max.nextInt(context));
            this.tester = (context, value) -> value >= min.nextInt(context) && value <= max.nextInt(context);
        }
    }

    public static BoundedIntUnaryOperator create(int value) {
        ConstantLootNumberProvider constantLootNumberProvider = ConstantLootNumberProvider.create(value);
        return new BoundedIntUnaryOperator(Optional.of(constantLootNumberProvider), Optional.of(constantLootNumberProvider));
    }

    public static BoundedIntUnaryOperator create(int min, int max) {
        return new BoundedIntUnaryOperator(Optional.of(ConstantLootNumberProvider.create(min)), Optional.of(ConstantLootNumberProvider.create(max)));
    }

    public static BoundedIntUnaryOperator createMin(int min) {
        return new BoundedIntUnaryOperator(Optional.of(ConstantLootNumberProvider.create(min)), Optional.empty());
    }

    public static BoundedIntUnaryOperator createMax(int max) {
        return new BoundedIntUnaryOperator(Optional.empty(), Optional.of(ConstantLootNumberProvider.create(max)));
    }

    public int apply(LootContext context, int value) {
        return this.applier.apply(context, value);
    }

    public boolean test(LootContext context, int value) {
        return this.tester.test(context, value);
    }

    private OptionalInt getConstantValue() {
        ConstantLootNumberProvider constantLootNumberProvider;
        LootNumberProvider lootNumberProvider;
        if (Objects.equals(this.min, this.max) && (lootNumberProvider = this.min) instanceof ConstantLootNumberProvider && Math.floor((constantLootNumberProvider = (ConstantLootNumberProvider)lootNumberProvider).value()) == (double)constantLootNumberProvider.value()) {
            return OptionalInt.of((int)constantLootNumberProvider.value());
        }
        return OptionalInt.empty();
    }

    @FunctionalInterface
    static interface Applier {
        public int apply(LootContext var1, int var2);
    }

    @FunctionalInterface
    static interface Tester {
        public boolean test(LootContext var1, int var2);
    }
}

