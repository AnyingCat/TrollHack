/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.command.ServerCommandSource;

public interface NumberRangeArgumentType<T extends NumberRange<?>>
extends ArgumentType<T> {
    public static IntRangeArgumentType intRange() {
        return new IntRangeArgumentType();
    }

    public static FloatRangeArgumentType floatRange() {
        return new FloatRangeArgumentType();
    }

    public static class IntRangeArgumentType
    implements NumberRangeArgumentType<NumberRange.IntRange> {
        private static final Collection<String> EXAMPLES = Arrays.asList("0..5", "0", "-5", "-100..", "..100");

        public static NumberRange.IntRange getRangeArgument(CommandContext<ServerCommandSource> context, String name) {
            return context.getArgument(name, NumberRange.IntRange.class);
        }

        @Override
        public NumberRange.IntRange parse(StringReader stringReader) throws CommandSyntaxException {
            return NumberRange.IntRange.parse(stringReader);
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        @Override
        public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
            return this.parse(reader);
        }
    }

    public static class FloatRangeArgumentType
    implements NumberRangeArgumentType<NumberRange.DoubleRange> {
        private static final Collection<String> EXAMPLES = Arrays.asList("0..5.2", "0", "-5.4", "-100.76..", "..100");

        public static NumberRange.DoubleRange getRangeArgument(CommandContext<ServerCommandSource> context, String name) {
            return context.getArgument(name, NumberRange.DoubleRange.class);
        }

        @Override
        public NumberRange.DoubleRange parse(StringReader stringReader) throws CommandSyntaxException {
            return NumberRange.DoubleRange.parse(stringReader);
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        @Override
        public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
            return this.parse(reader);
        }
    }
}

