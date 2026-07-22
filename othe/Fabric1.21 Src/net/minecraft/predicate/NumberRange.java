/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.text.Text;

public interface NumberRange<T extends Number> {
    public static final SimpleCommandExceptionType EXCEPTION_EMPTY = new SimpleCommandExceptionType(Text.translatable("argument.range.empty"));
    public static final SimpleCommandExceptionType EXCEPTION_SWAPPED = new SimpleCommandExceptionType(Text.translatable("argument.range.swapped"));

    public Optional<T> min();

    public Optional<T> max();

    default public boolean isDummy() {
        return this.min().isEmpty() && this.max().isEmpty();
    }

    default public Optional<T> getConstantValue() {
        Optional<T> optional2;
        Optional<T> optional = this.min();
        return optional.equals(optional2 = this.max()) ? optional : Optional.empty();
    }

    public static <T extends Number, R extends NumberRange<T>> Codec<R> createCodec(Codec<T> valueCodec, Factory<T, R> rangeFactory) {
        Codec codec = RecordCodecBuilder.create(instance -> instance.group(valueCodec.optionalFieldOf("min").forGetter(NumberRange::min), valueCodec.optionalFieldOf("max").forGetter(NumberRange::max)).apply((Applicative<NumberRange, ?>)instance, rangeFactory::create));
        return Codec.either(codec, valueCodec).xmap(either -> either.map(range -> range, value -> rangeFactory.create(Optional.of(value), Optional.of(value))), range -> {
            Optional optional = range.getConstantValue();
            return optional.isPresent() ? Either.right((Number)optional.get()) : Either.left(range);
        });
    }

    public static <T extends Number, R extends NumberRange<T>> R parse(StringReader commandReader, CommandFactory<T, R> commandFactory, Function<String, T> converter, Supplier<DynamicCommandExceptionType> exceptionTypeSupplier, Function<T, T> mapper) throws CommandSyntaxException {
        if (!commandReader.canRead()) {
            throw EXCEPTION_EMPTY.createWithContext(commandReader);
        }
        int i = commandReader.getCursor();
        try {
            Optional<T> optional2;
            Optional<T> optional = NumberRange.fromStringReader(commandReader, converter, exceptionTypeSupplier).map(mapper);
            if (commandReader.canRead(2) && commandReader.peek() == '.' && commandReader.peek(1) == '.') {
                commandReader.skip();
                commandReader.skip();
                optional2 = NumberRange.fromStringReader(commandReader, converter, exceptionTypeSupplier).map(mapper);
                if (optional.isEmpty() && optional2.isEmpty()) {
                    throw EXCEPTION_EMPTY.createWithContext(commandReader);
                }
            } else {
                optional2 = optional;
            }
            if (optional.isEmpty() && optional2.isEmpty()) {
                throw EXCEPTION_EMPTY.createWithContext(commandReader);
            }
            return commandFactory.create(commandReader, optional, optional2);
        } catch (CommandSyntaxException commandSyntaxException) {
            commandReader.setCursor(i);
            throw new CommandSyntaxException(commandSyntaxException.getType(), commandSyntaxException.getRawMessage(), commandSyntaxException.getInput(), i);
        }
    }

    private static <T extends Number> Optional<T> fromStringReader(StringReader reader, Function<String, T> converter, Supplier<DynamicCommandExceptionType> exceptionTypeSupplier) throws CommandSyntaxException {
        int i = reader.getCursor();
        while (reader.canRead() && NumberRange.isNextCharValid(reader)) {
            reader.skip();
        }
        String string = reader.getString().substring(i, reader.getCursor());
        if (string.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of((Number)converter.apply(string));
        } catch (NumberFormatException numberFormatException) {
            throw exceptionTypeSupplier.get().createWithContext(reader, string);
        }
    }

    private static boolean isNextCharValid(StringReader reader) {
        char c = reader.peek();
        if (c >= '0' && c <= '9' || c == '-') {
            return true;
        }
        if (c == '.') {
            return !reader.canRead(2) || reader.peek(1) != '.';
        }
        return false;
    }

    @FunctionalInterface
    public static interface Factory<T extends Number, R extends NumberRange<T>> {
        public R create(Optional<T> var1, Optional<T> var2);
    }

    @FunctionalInterface
    public static interface CommandFactory<T extends Number, R extends NumberRange<T>> {
        public R create(StringReader var1, Optional<T> var2, Optional<T> var3) throws CommandSyntaxException;
    }

    public record DoubleRange(Optional<Double> min, Optional<Double> max, Optional<Double> squaredMin, Optional<Double> squaredMax) implements NumberRange<Double>
    {
        public static final DoubleRange ANY = new DoubleRange(Optional.empty(), Optional.empty());
        public static final Codec<DoubleRange> CODEC = NumberRange.createCodec(Codec.DOUBLE, DoubleRange::new);

        private DoubleRange(Optional<Double> min, Optional<Double> max) {
            this(min, max, DoubleRange.square(min), DoubleRange.square(max));
        }

        private static DoubleRange create(StringReader reader, Optional<Double> min, Optional<Double> max) throws CommandSyntaxException {
            if (min.isPresent() && max.isPresent() && min.get() > max.get()) {
                throw EXCEPTION_SWAPPED.createWithContext(reader);
            }
            return new DoubleRange(min, max);
        }

        private static Optional<Double> square(Optional<Double> value) {
            return value.map(d -> d * d);
        }

        public static DoubleRange exactly(double value) {
            return new DoubleRange(Optional.of(value), Optional.of(value));
        }

        public static DoubleRange between(double min, double max) {
            return new DoubleRange(Optional.of(min), Optional.of(max));
        }

        public static DoubleRange atLeast(double value) {
            return new DoubleRange(Optional.of(value), Optional.empty());
        }

        public static DoubleRange atMost(double value) {
            return new DoubleRange(Optional.empty(), Optional.of(value));
        }

        public boolean test(double value) {
            if (this.min.isPresent() && this.min.get() > value) {
                return false;
            }
            return this.max.isEmpty() || !(this.max.get() < value);
        }

        public boolean testSqrt(double value) {
            if (this.squaredMin.isPresent() && this.squaredMin.get() > value) {
                return false;
            }
            return this.squaredMax.isEmpty() || !(this.squaredMax.get() < value);
        }

        public static DoubleRange parse(StringReader reader) throws CommandSyntaxException {
            return DoubleRange.parse(reader, value -> value);
        }

        public static DoubleRange parse(StringReader reader, Function<Double, Double> mapper) throws CommandSyntaxException {
            return NumberRange.parse(reader, DoubleRange::create, Double::parseDouble, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidDouble, mapper);
        }
    }

    public record IntRange(Optional<Integer> min, Optional<Integer> max, Optional<Long> minSquared, Optional<Long> maxSquared) implements NumberRange<Integer>
    {
        public static final IntRange ANY = new IntRange(Optional.empty(), Optional.empty());
        public static final Codec<IntRange> CODEC = NumberRange.createCodec(Codec.INT, IntRange::new);

        private IntRange(Optional<Integer> min, Optional<Integer> max) {
            this(min, max, min.map(i -> i.longValue() * i.longValue()), IntRange.square(max));
        }

        private static IntRange parse(StringReader reader, Optional<Integer> min, Optional<Integer> max) throws CommandSyntaxException {
            if (min.isPresent() && max.isPresent() && min.get() > max.get()) {
                throw EXCEPTION_SWAPPED.createWithContext(reader);
            }
            return new IntRange(min, max);
        }

        private static Optional<Long> square(Optional<Integer> value) {
            return value.map(i -> i.longValue() * i.longValue());
        }

        public static IntRange exactly(int value) {
            return new IntRange(Optional.of(value), Optional.of(value));
        }

        public static IntRange between(int min, int max) {
            return new IntRange(Optional.of(min), Optional.of(max));
        }

        public static IntRange atLeast(int value) {
            return new IntRange(Optional.of(value), Optional.empty());
        }

        public static IntRange atMost(int value) {
            return new IntRange(Optional.empty(), Optional.of(value));
        }

        public boolean test(int value) {
            if (this.min.isPresent() && this.min.get() > value) {
                return false;
            }
            return this.max.isEmpty() || this.max.get() >= value;
        }

        public boolean testSqrt(long value) {
            if (this.minSquared.isPresent() && this.minSquared.get() > value) {
                return false;
            }
            return this.maxSquared.isEmpty() || this.maxSquared.get() >= value;
        }

        public static IntRange parse(StringReader reader) throws CommandSyntaxException {
            return IntRange.fromStringReader(reader, value -> value);
        }

        public static IntRange fromStringReader(StringReader reader, Function<Integer, Integer> converter) throws CommandSyntaxException {
            return NumberRange.parse(reader, IntRange::parse, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt, converter);
        }
    }
}

