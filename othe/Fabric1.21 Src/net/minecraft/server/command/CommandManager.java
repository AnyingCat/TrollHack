/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.command.argument.ArgumentHelper;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.command.AdvancementCommand;
import net.minecraft.server.command.AttributeCommand;
import net.minecraft.server.command.BossBarCommand;
import net.minecraft.server.command.ClearCommand;
import net.minecraft.server.command.CloneCommand;
import net.minecraft.server.command.DamageCommand;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.DatapackCommand;
import net.minecraft.server.command.DebugCommand;
import net.minecraft.server.command.DebugConfigCommand;
import net.minecraft.server.command.DebugMobSpawningCommand;
import net.minecraft.server.command.DebugPathCommand;
import net.minecraft.server.command.DefaultGameModeCommand;
import net.minecraft.server.command.DifficultyCommand;
import net.minecraft.server.command.EffectCommand;
import net.minecraft.server.command.EnchantCommand;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ExperienceCommand;
import net.minecraft.server.command.FillBiomeCommand;
import net.minecraft.server.command.FillCommand;
import net.minecraft.server.command.ForceLoadCommand;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.GameModeCommand;
import net.minecraft.server.command.GameRuleCommand;
import net.minecraft.server.command.GiveCommand;
import net.minecraft.server.command.HelpCommand;
import net.minecraft.server.command.ItemCommand;
import net.minecraft.server.command.JfrCommand;
import net.minecraft.server.command.KickCommand;
import net.minecraft.server.command.KillCommand;
import net.minecraft.server.command.ListCommand;
import net.minecraft.server.command.LocateCommand;
import net.minecraft.server.command.LootCommand;
import net.minecraft.server.command.MeCommand;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ParticleCommand;
import net.minecraft.server.command.PlaceCommand;
import net.minecraft.server.command.PlaySoundCommand;
import net.minecraft.server.command.PublishCommand;
import net.minecraft.server.command.RaidCommand;
import net.minecraft.server.command.RandomCommand;
import net.minecraft.server.command.RecipeCommand;
import net.minecraft.server.command.ReloadCommand;
import net.minecraft.server.command.ReturnCommand;
import net.minecraft.server.command.RideCommand;
import net.minecraft.server.command.SayCommand;
import net.minecraft.server.command.ScheduleCommand;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.server.command.SeedCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.ServerPackCommand;
import net.minecraft.server.command.SetBlockCommand;
import net.minecraft.server.command.SetWorldSpawnCommand;
import net.minecraft.server.command.SpawnArmorTrimsCommand;
import net.minecraft.server.command.SpawnPointCommand;
import net.minecraft.server.command.SpectateCommand;
import net.minecraft.server.command.SpreadPlayersCommand;
import net.minecraft.server.command.StopSoundCommand;
import net.minecraft.server.command.SummonCommand;
import net.minecraft.server.command.TagCommand;
import net.minecraft.server.command.TeamCommand;
import net.minecraft.server.command.TeamMsgCommand;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.command.TellRawCommand;
import net.minecraft.server.command.TestCommand;
import net.minecraft.server.command.TickCommand;
import net.minecraft.server.command.TimeCommand;
import net.minecraft.server.command.TitleCommand;
import net.minecraft.server.command.TriggerCommand;
import net.minecraft.server.command.WardenSpawnTrackerCommand;
import net.minecraft.server.command.WeatherCommand;
import net.minecraft.server.command.WorldBorderCommand;
import net.minecraft.server.dedicated.command.BanCommand;
import net.minecraft.server.dedicated.command.BanIpCommand;
import net.minecraft.server.dedicated.command.BanListCommand;
import net.minecraft.server.dedicated.command.DeOpCommand;
import net.minecraft.server.dedicated.command.OpCommand;
import net.minecraft.server.dedicated.command.PardonCommand;
import net.minecraft.server.dedicated.command.PardonIpCommand;
import net.minecraft.server.dedicated.command.PerfCommand;
import net.minecraft.server.dedicated.command.SaveAllCommand;
import net.minecraft.server.dedicated.command.SaveOffCommand;
import net.minecraft.server.dedicated.command.SaveOnCommand;
import net.minecraft.server.dedicated.command.SetIdleTimeoutCommand;
import net.minecraft.server.dedicated.command.StopCommand;
import net.minecraft.server.dedicated.command.TransferCommand;
import net.minecraft.server.dedicated.command.WhitelistCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class CommandManager {
    private static final ThreadLocal<CommandExecutionContext<ServerCommandSource>> CURRENT_CONTEXT = new ThreadLocal();
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int field_31837 = 0;
    public static final int field_31838 = 1;
    public static final int field_31839 = 2;
    public static final int field_31840 = 3;
    public static final int field_31841 = 4;
    private final CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher();

    public CommandManager(RegistrationEnvironment environment, CommandRegistryAccess commandRegistryAccess) {
        AdvancementCommand.register(this.dispatcher);
        AttributeCommand.register(this.dispatcher, commandRegistryAccess);
        ExecuteCommand.register(this.dispatcher, commandRegistryAccess);
        BossBarCommand.register(this.dispatcher, commandRegistryAccess);
        ClearCommand.register(this.dispatcher, commandRegistryAccess);
        CloneCommand.register(this.dispatcher, commandRegistryAccess);
        DamageCommand.register(this.dispatcher, commandRegistryAccess);
        DataCommand.register(this.dispatcher);
        DatapackCommand.register(this.dispatcher);
        DebugCommand.register(this.dispatcher);
        DefaultGameModeCommand.register(this.dispatcher);
        DifficultyCommand.register(this.dispatcher);
        EffectCommand.register(this.dispatcher, commandRegistryAccess);
        MeCommand.register(this.dispatcher);
        EnchantCommand.register(this.dispatcher, commandRegistryAccess);
        ExperienceCommand.register(this.dispatcher);
        FillCommand.register(this.dispatcher, commandRegistryAccess);
        FillBiomeCommand.register(this.dispatcher, commandRegistryAccess);
        ForceLoadCommand.register(this.dispatcher);
        FunctionCommand.register(this.dispatcher);
        GameModeCommand.register(this.dispatcher);
        GameRuleCommand.register(this.dispatcher);
        GiveCommand.register(this.dispatcher, commandRegistryAccess);
        HelpCommand.register(this.dispatcher);
        ItemCommand.register(this.dispatcher, commandRegistryAccess);
        KickCommand.register(this.dispatcher);
        KillCommand.register(this.dispatcher);
        ListCommand.register(this.dispatcher);
        LocateCommand.register(this.dispatcher, commandRegistryAccess);
        LootCommand.register(this.dispatcher, commandRegistryAccess);
        MessageCommand.register(this.dispatcher);
        ParticleCommand.register(this.dispatcher, commandRegistryAccess);
        PlaceCommand.register(this.dispatcher);
        PlaySoundCommand.register(this.dispatcher);
        RandomCommand.register(this.dispatcher);
        ReloadCommand.register(this.dispatcher);
        RecipeCommand.register(this.dispatcher);
        ReturnCommand.register(this.dispatcher);
        RideCommand.register(this.dispatcher);
        SayCommand.register(this.dispatcher);
        ScheduleCommand.register(this.dispatcher);
        ScoreboardCommand.register(this.dispatcher, commandRegistryAccess);
        SeedCommand.register(this.dispatcher, environment != RegistrationEnvironment.INTEGRATED);
        SetBlockCommand.register(this.dispatcher, commandRegistryAccess);
        SpawnPointCommand.register(this.dispatcher);
        SetWorldSpawnCommand.register(this.dispatcher);
        SpectateCommand.register(this.dispatcher);
        SpreadPlayersCommand.register(this.dispatcher);
        StopSoundCommand.register(this.dispatcher);
        SummonCommand.register(this.dispatcher, commandRegistryAccess);
        TagCommand.register(this.dispatcher);
        TeamCommand.register(this.dispatcher, commandRegistryAccess);
        TeamMsgCommand.register(this.dispatcher);
        TeleportCommand.register(this.dispatcher);
        TellRawCommand.register(this.dispatcher, commandRegistryAccess);
        TickCommand.register(this.dispatcher);
        TimeCommand.register(this.dispatcher);
        TitleCommand.register(this.dispatcher, commandRegistryAccess);
        TriggerCommand.register(this.dispatcher);
        WeatherCommand.register(this.dispatcher);
        WorldBorderCommand.register(this.dispatcher);
        if (FlightProfiler.INSTANCE.isAvailable()) {
            JfrCommand.register(this.dispatcher);
        }
        if (SharedConstants.isDevelopment) {
            TestCommand.register(this.dispatcher);
            RaidCommand.register(this.dispatcher, commandRegistryAccess);
            DebugPathCommand.register(this.dispatcher);
            DebugMobSpawningCommand.register(this.dispatcher);
            WardenSpawnTrackerCommand.register(this.dispatcher);
            SpawnArmorTrimsCommand.register(this.dispatcher);
            ServerPackCommand.register(this.dispatcher);
            if (environment.dedicated) {
                DebugConfigCommand.register(this.dispatcher);
            }
        }
        if (environment.dedicated) {
            BanIpCommand.register(this.dispatcher);
            BanListCommand.register(this.dispatcher);
            BanCommand.register(this.dispatcher);
            DeOpCommand.register(this.dispatcher);
            OpCommand.register(this.dispatcher);
            PardonCommand.register(this.dispatcher);
            PardonIpCommand.register(this.dispatcher);
            PerfCommand.register(this.dispatcher);
            SaveAllCommand.register(this.dispatcher);
            SaveOffCommand.register(this.dispatcher);
            SaveOnCommand.register(this.dispatcher);
            SetIdleTimeoutCommand.register(this.dispatcher);
            StopCommand.register(this.dispatcher);
            TransferCommand.register(this.dispatcher);
            WhitelistCommand.register(this.dispatcher);
        }
        if (environment.integrated) {
            PublishCommand.register(this.dispatcher);
        }
        this.dispatcher.setConsumer(AbstractServerCommandSource.asResultConsumer());
    }

    /**
     * {@return {@code parseResults} with {@code sourceMapper} applied to the
     * command source}
     */
    public static <S> ParseResults<S> withCommandSource(ParseResults<S> parseResults, UnaryOperator<S> sourceMapper) {
        CommandContextBuilder commandContextBuilder = parseResults.getContext();
        CommandContextBuilder<S> commandContextBuilder2 = commandContextBuilder.withSource(sourceMapper.apply(commandContextBuilder.getSource()));
        return new ParseResults<S>(commandContextBuilder2, parseResults.getReader(), parseResults.getExceptions());
    }

    /**
     * Executes {@code command}. Unlike {@link #execute} the command can be prefixed
     * with a slash.
     */
    public void executeWithPrefix(ServerCommandSource source, String command) {
        command = command.startsWith("/") ? command.substring(1) : command;
        this.execute(this.dispatcher.parse(command, source), command);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    /**
     * Executes {@code command}. The command cannot be prefixed with a slash.
     * 
     * @see #executeWithPrefix(ServerCommandSource, String)
     */
    public void execute(ParseResults<ServerCommandSource> parseResults, String command) {
        ServerCommandSource serverCommandSource = parseResults.getContext().getSource();
        serverCommandSource.getServer().getProfiler().push(() -> "/" + command);
        ContextChain<ServerCommandSource> contextChain = CommandManager.checkCommand(parseResults, command, serverCommandSource);
        try {
            if (contextChain != null) {
                CommandManager.callWithContext(serverCommandSource, context -> CommandExecutionContext.enqueueCommand(context, command, contextChain, serverCommandSource, ReturnValueConsumer.EMPTY));
            }
        } catch (Exception exception) {
            MutableText mutableText = Text.literal(exception.getMessage() == null ? exception.getClass().getName() : exception.getMessage());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Command exception: /{}", (Object)command, (Object)exception);
                StackTraceElement[] stackTraceElements = exception.getStackTrace();
                for (int i = 0; i < Math.min(stackTraceElements.length, 3); ++i) {
                    mutableText.append("\n\n").append(stackTraceElements[i].getMethodName()).append("\n ").append(stackTraceElements[i].getFileName()).append(":").append(String.valueOf(stackTraceElements[i].getLineNumber()));
                }
            }
            serverCommandSource.sendError(Text.translatable("command.failed").styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mutableText))));
            if (SharedConstants.isDevelopment) {
                serverCommandSource.sendError(Text.literal(Util.getInnermostMessage(exception)));
                LOGGER.error("'/{}' threw an exception", (Object)command, (Object)exception);
            }
        } finally {
            serverCommandSource.getServer().getProfiler().pop();
        }
    }

    @Nullable
    private static ContextChain<ServerCommandSource> checkCommand(ParseResults<ServerCommandSource> parseResults, String command, ServerCommandSource source) {
        try {
            CommandManager.throwException(parseResults);
            return ContextChain.tryFlatten(parseResults.getContext().build(command)).orElseThrow(() -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader()));
        } catch (CommandSyntaxException commandSyntaxException) {
            source.sendError(Texts.toText(commandSyntaxException.getRawMessage()));
            if (commandSyntaxException.getInput() != null && commandSyntaxException.getCursor() >= 0) {
                int i = Math.min(commandSyntaxException.getInput().length(), commandSyntaxException.getCursor());
                MutableText mutableText = Text.empty().formatted(Formatting.GRAY).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + command)));
                if (i > 10) {
                    mutableText.append(ScreenTexts.ELLIPSIS);
                }
                mutableText.append(commandSyntaxException.getInput().substring(Math.max(0, i - 10), i));
                if (i < commandSyntaxException.getInput().length()) {
                    MutableText text = Text.literal(commandSyntaxException.getInput().substring(i)).formatted(Formatting.RED, Formatting.UNDERLINE);
                    mutableText.append(text);
                }
                mutableText.append(Text.translatable("command.context.here").formatted(Formatting.RED, Formatting.ITALIC));
                source.sendError(mutableText);
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void callWithContext(ServerCommandSource commandSource, Consumer<CommandExecutionContext<ServerCommandSource>> callback) {
        block9: {
            boolean bl;
            MinecraftServer minecraftServer = commandSource.getServer();
            CommandExecutionContext<ServerCommandSource> commandExecutionContext = CURRENT_CONTEXT.get();
            boolean bl2 = bl = commandExecutionContext == null;
            if (bl) {
                int i = Math.max(1, minecraftServer.getGameRules().getInt(GameRules.MAX_COMMAND_CHAIN_LENGTH));
                int j = minecraftServer.getGameRules().getInt(GameRules.MAX_COMMAND_FORK_COUNT);
                try (CommandExecutionContext commandExecutionContext2 = new CommandExecutionContext(i, j, minecraftServer.getProfiler());){
                    CURRENT_CONTEXT.set(commandExecutionContext2);
                    callback.accept(commandExecutionContext2);
                    commandExecutionContext2.run();
                    break block9;
                } finally {
                    CURRENT_CONTEXT.set(null);
                }
            }
            callback.accept(commandExecutionContext);
        }
    }

    public void sendCommandTree(ServerPlayerEntity player) {
        HashMap<CommandNode<ServerCommandSource>, CommandNode<CommandSource>> map = Maps.newHashMap();
        RootCommandNode<CommandSource> rootCommandNode = new RootCommandNode<CommandSource>();
        map.put(this.dispatcher.getRoot(), rootCommandNode);
        this.makeTreeForSource(this.dispatcher.getRoot(), rootCommandNode, player.getCommandSource(), map);
        player.networkHandler.sendPacket(new CommandTreeS2CPacket(rootCommandNode));
    }

    private void makeTreeForSource(CommandNode<ServerCommandSource> tree, CommandNode<CommandSource> result, ServerCommandSource source, Map<CommandNode<ServerCommandSource>, CommandNode<CommandSource>> resultNodes) {
        for (CommandNode<ServerCommandSource> commandNode : tree.getChildren()) {
            RequiredArgumentBuilder requiredArgumentBuilder;
            if (!commandNode.canUse(source)) continue;
            ArgumentBuilder<ServerCommandSource, ?> argumentBuilder = commandNode.createBuilder();
            argumentBuilder.requires(sourcex -> true);
            if (argumentBuilder.getCommand() != null) {
                argumentBuilder.executes(context -> 0);
            }
            if (argumentBuilder instanceof RequiredArgumentBuilder && (requiredArgumentBuilder = (RequiredArgumentBuilder)argumentBuilder).getSuggestionsProvider() != null) {
                requiredArgumentBuilder.suggests(SuggestionProviders.getLocalProvider(requiredArgumentBuilder.getSuggestionsProvider()));
            }
            if (argumentBuilder.getRedirect() != null) {
                argumentBuilder.redirect(resultNodes.get(argumentBuilder.getRedirect()));
            }
            CommandNode<CommandSource> commandNode2 = argumentBuilder.build();
            resultNodes.put(commandNode, commandNode2);
            result.addChild(commandNode2);
            if (commandNode.getChildren().isEmpty()) continue;
            this.makeTreeForSource(commandNode, commandNode2, source, resultNodes);
        }
    }

    public static LiteralArgumentBuilder<ServerCommandSource> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    public static <T> RequiredArgumentBuilder<ServerCommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static Predicate<String> getCommandValidator(CommandParser parser) {
        return string -> {
            try {
                parser.parse(new StringReader((String)string));
                return true;
            } catch (CommandSyntaxException commandSyntaxException) {
                return false;
            }
        };
    }

    public CommandDispatcher<ServerCommandSource> getDispatcher() {
        return this.dispatcher;
    }

    public static <S> void throwException(ParseResults<S> parse) throws CommandSyntaxException {
        CommandSyntaxException commandSyntaxException = CommandManager.getException(parse);
        if (commandSyntaxException != null) {
            throw commandSyntaxException;
        }
    }

    @Nullable
    public static <S> CommandSyntaxException getException(ParseResults<S> parse) {
        if (!parse.getReader().canRead()) {
            return null;
        }
        if (parse.getExceptions().size() == 1) {
            return parse.getExceptions().values().iterator().next();
        }
        if (parse.getContext().getRange().isEmpty()) {
            return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parse.getReader());
        }
        return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(parse.getReader());
    }

    public static CommandRegistryAccess createRegistryAccess(final RegistryWrapper.WrapperLookup registryLookup) {
        return new CommandRegistryAccess(){

            @Override
            public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
                return registryLookup.streamAllRegistryKeys();
            }

            @Override
            public <T> Optional<RegistryWrapper.Impl<T>> getOptionalWrapper(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return registryLookup.getOptionalWrapper(registryRef).map(this::createTagCreatingLookup);
            }

            private <T> RegistryWrapper.Impl.Delegating<T> createTagCreatingLookup(final RegistryWrapper.Impl<T> original) {
                return new RegistryWrapper.Impl.Delegating<T>(this){

                    @Override
                    public RegistryWrapper.Impl<T> getBase() {
                        return original;
                    }

                    @Override
                    public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
                        return Optional.of(this.getOrThrow(tag));
                    }

                    @Override
                    public RegistryEntryList.Named<T> getOrThrow(TagKey<T> tag) {
                        Optional<RegistryEntryList.Named<RegistryEntryList.Named>> optional = this.getBase().getOptional(tag);
                        return optional.orElseGet(() -> RegistryEntryList.of(this.getBase(), tag));
                    }
                };
            }
        };
    }

    public static void checkMissing() {
        CommandRegistryAccess commandRegistryAccess = CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup());
        CommandDispatcher<ServerCommandSource> commandDispatcher = new CommandManager(RegistrationEnvironment.ALL, commandRegistryAccess).getDispatcher();
        RootCommandNode<ServerCommandSource> rootCommandNode = commandDispatcher.getRoot();
        commandDispatcher.findAmbiguities((parent, child, sibling, inputs) -> LOGGER.warn("Ambiguity between arguments {} and {} with inputs: {}", commandDispatcher.getPath(child), commandDispatcher.getPath(sibling), inputs));
        Set<ArgumentType<?>> set = ArgumentHelper.collectUsedArgumentTypes(rootCommandNode);
        Set set2 = set.stream().filter(type -> !ArgumentTypes.has(type.getClass())).collect(Collectors.toSet());
        if (!set2.isEmpty()) {
            LOGGER.warn("Missing type registration for following arguments:\n {}", (Object)set2.stream().map(type -> "\t" + String.valueOf(type)).collect(Collectors.joining(",\n")));
            throw new IllegalStateException("Unregistered argument types");
        }
    }

    public static enum RegistrationEnvironment {
        ALL(true, true),
        DEDICATED(false, true),
        INTEGRATED(true, false);

        public final boolean integrated;
        public final boolean dedicated;

        private RegistrationEnvironment(boolean integrated, boolean dedicated) {
            this.integrated = integrated;
            this.dedicated = dedicated;
        }
    }

    @FunctionalInterface
    public static interface CommandParser {
        public void parse(StringReader var1) throws CommandSyntaxException;
    }
}

