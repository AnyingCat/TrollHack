/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class OperationArgumentType
implements ArgumentType<Operation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("=", ">", "<");
    private static final SimpleCommandExceptionType INVALID_OPERATION = new SimpleCommandExceptionType(Text.translatable("arguments.operation.invalid"));
    private static final SimpleCommandExceptionType DIVISION_ZERO_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("arguments.operation.div0"));

    public static OperationArgumentType operation() {
        return new OperationArgumentType();
    }

    public static Operation getOperation(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, Operation.class);
    }

    @Override
    public Operation parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead()) {
            int i = stringReader.getCursor();
            while (stringReader.canRead() && stringReader.peek() != ' ') {
                stringReader.skip();
            }
            return OperationArgumentType.getOperator(stringReader.getString().substring(i, stringReader.getCursor()));
        }
        throw INVALID_OPERATION.createWithContext(stringReader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static Operation getOperator(String operator) throws CommandSyntaxException {
        if (operator.equals("><")) {
            return (a, b) -> {
                int i = a.getScore();
                a.setScore(b.getScore());
                b.setScore(i);
            };
        }
        return OperationArgumentType.getIntOperator(operator);
    }

    private static IntOperator getIntOperator(String operator) throws CommandSyntaxException {
        return switch (operator) {
            case "=" -> (a, b) -> b;
            case "+=" -> Integer::sum;
            case "-=" -> (a, b) -> a - b;
            case "*=" -> (a, b) -> a * b;
            case "/=" -> (a, b) -> {
                if (b == 0) {
                    throw DIVISION_ZERO_EXCEPTION.create();
                }
                return MathHelper.floorDiv(a, b);
            };
            case "%=" -> (a, b) -> {
                if (b == 0) {
                    throw DIVISION_ZERO_EXCEPTION.create();
                }
                return MathHelper.floorMod(a, b);
            };
            case "<" -> Math::min;
            case ">" -> Math::max;
            default -> throw INVALID_OPERATION.create();
        };
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    @FunctionalInterface
    public static interface Operation {
        public void apply(ScoreAccess var1, ScoreAccess var2) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface IntOperator
    extends Operation {
        public int apply(int var1, int var2) throws CommandSyntaxException;

        @Override
        default public void apply(ScoreAccess scoreAccess, ScoreAccess scoreAccess2) throws CommandSyntaxException {
            scoreAccess.setScore(this.apply(scoreAccess.getScore(), scoreAccess2.getScore()));
        }
    }
}

