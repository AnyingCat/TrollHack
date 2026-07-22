/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NumberRangeArgumentType;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSequencesState;
import org.jetbrains.annotations.Nullable;

public class RandomCommand {
    private static final SimpleCommandExceptionType RANGE_TOO_LARGE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.random.error.range_too_large"));
    private static final SimpleCommandExceptionType RANGE_TOO_SMALL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.random.error.range_too_small"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("random").then(RandomCommand.random("value", false))).then(RandomCommand.random("roll", true))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("reset").requires(source -> source.hasPermissionLevel(2))).then(((LiteralArgumentBuilder)CommandManager.literal("*").executes(context -> RandomCommand.executeReset((ServerCommandSource)context.getSource()))).then(((RequiredArgumentBuilder)CommandManager.argument("seed", IntegerArgumentType.integer()).executes(context -> RandomCommand.executeReset((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "seed"), true, true))).then(((RequiredArgumentBuilder)CommandManager.argument("includeWorldSeed", BoolArgumentType.bool()).executes(context -> RandomCommand.executeReset((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "seed"), BoolArgumentType.getBool(context, "includeWorldSeed"), true))).then(CommandManager.argument("includeSequenceId", BoolArgumentType.bool()).executes(context -> RandomCommand.executeReset((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "seed"), BoolArgumentType.getBool(context, "includeWorldSeed"), BoolArgumentType.getBool(context, "includeSequenceId")))))))).then(((RequiredArgumentBuilder)CommandManager.argument("sequence", IdentifierArgumentType.identifier()).suggests(RandomCommand::suggestSequences).executes(context -> RandomCommand.executeReset((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "sequence")))).then(((RequiredArgumentBuilder)CommandManager.argument("seed", IntegerArgumentType.integer()).executes(context -> RandomCommand.executeReset((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "sequence"), IntegerArgumentType.getInteger(context, "seed"), true, true))).then(((RequiredArgumentBuilder)CommandManager.argument("includeWorldSeed", BoolArgumentType.bool()).executes(context -> RandomCommand.executeReset((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "sequence"), IntegerArgumentType.getInteger(context, "seed"), BoolArgumentType.getBool(context, "includeWorldSeed"), true))).then(CommandManager.argument("includeSequenceId", BoolArgumentType.bool()).executes(context -> RandomCommand.executeReset((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "sequence"), IntegerArgumentType.getInteger(context, "seed"), BoolArgumentType.getBool(context, "includeWorldSeed"), BoolArgumentType.getBool(context, "includeSequenceId")))))))));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> random(String argumentName, boolean roll) {
        return (LiteralArgumentBuilder)CommandManager.literal(argumentName).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("range", NumberRangeArgumentType.intRange()).executes(context -> RandomCommand.execute((ServerCommandSource)context.getSource(), NumberRangeArgumentType.IntRangeArgumentType.getRangeArgument(context, "range"), null, roll))).then(((RequiredArgumentBuilder)CommandManager.argument("sequence", IdentifierArgumentType.identifier()).suggests(RandomCommand::suggestSequences).requires(source -> source.hasPermissionLevel(2))).executes(context -> RandomCommand.execute((ServerCommandSource)context.getSource(), NumberRangeArgumentType.IntRangeArgumentType.getRangeArgument(context, "range"), IdentifierArgumentType.getIdentifier(context, "sequence"), roll))));
    }

    private static CompletableFuture<Suggestions> suggestSequences(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestionsBuilder) {
        ArrayList<String> list = Lists.newArrayList();
        context.getSource().getWorld().getRandomSequences().forEachSequence((id, sequence) -> list.add(id.toString()));
        return CommandSource.suggestMatching(list, suggestionsBuilder);
    }

    private static int execute(ServerCommandSource source, NumberRange.IntRange range, @Nullable Identifier sequenceId, boolean roll) throws CommandSyntaxException {
        Random random = sequenceId != null ? source.getWorld().getOrCreateRandom(sequenceId) : source.getWorld().getRandom();
        int i = range.min().orElse(Integer.MIN_VALUE);
        int j = range.max().orElse(Integer.MAX_VALUE);
        long l = (long)j - (long)i;
        if (l == 0L) {
            throw RANGE_TOO_SMALL_EXCEPTION.create();
        }
        if (l >= Integer.MAX_VALUE) {
            throw RANGE_TOO_LARGE_EXCEPTION.create();
        }
        int k = MathHelper.nextBetween(random, i, j);
        if (roll) {
            source.getServer().getPlayerManager().broadcast(Text.translatable("commands.random.roll", source.getDisplayName(), k, i, j), false);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.random.sample.success", k), false);
        }
        return k;
    }

    private static int executeReset(ServerCommandSource source, Identifier sequenceId) throws CommandSyntaxException {
        source.getWorld().getRandomSequences().reset(sequenceId);
        source.sendFeedback(() -> Text.translatable("commands.random.reset.success", Text.of(sequenceId)), false);
        return 1;
    }

    private static int executeReset(ServerCommandSource source, Identifier sequenceId, int salt, boolean includeWorldSeed, boolean includeSequenceId) throws CommandSyntaxException {
        source.getWorld().getRandomSequences().reset(sequenceId, salt, includeWorldSeed, includeSequenceId);
        source.sendFeedback(() -> Text.translatable("commands.random.reset.success", Text.of(sequenceId)), false);
        return 1;
    }

    private static int executeReset(ServerCommandSource source) {
        int i = source.getWorld().getRandomSequences().resetAll();
        source.sendFeedback(() -> Text.translatable("commands.random.reset.all.success", i), false);
        return i;
    }

    private static int executeReset(ServerCommandSource source, int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        RandomSequencesState randomSequencesState = source.getWorld().getRandomSequences();
        randomSequencesState.setDefaultParameters(salt, includeWorldSeed, includeSequenceId);
        int i = randomSequencesState.resetAll();
        source.sendFeedback(() -> Text.translatable("commands.random.reset.all.success", i), false);
        return i;
    }
}

