/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import net.minecraft.command.CommandFunctionAction;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ControlFlowAware;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.ExecutionControl;
import net.minecraft.command.ExecutionFlags;
import net.minecraft.command.FallthroughCommandAction;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.function.MacroException;
import net.minecraft.server.function.Procedure;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class FunctionCommand {
    private static final DynamicCommandExceptionType ARGUMENT_NOT_COMPOUND_EXCEPTION = new DynamicCommandExceptionType(argument -> Text.stringifiedTranslatable("commands.function.error.argument_not_compound", argument));
    static final DynamicCommandExceptionType NO_FUNCTIONS_EXCEPTION = new DynamicCommandExceptionType(argument -> Text.stringifiedTranslatable("commands.function.scheduled.no_functions", argument));
    @VisibleForTesting
    public static final Dynamic2CommandExceptionType INSTANTIATION_FAILURE_EXCEPTION = new Dynamic2CommandExceptionType((argument, argument2) -> Text.stringifiedTranslatable("commands.function.instantiationFailure", argument, argument2));
    public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
        CommandFunctionManager commandFunctionManager = ((ServerCommandSource)context.getSource()).getServer().getCommandFunctionManager();
        CommandSource.suggestIdentifiers(commandFunctionManager.getFunctionTags(), builder, "#");
        return CommandSource.suggestIdentifiers(commandFunctionManager.getAllFunctions(), builder);
    };
    static final ResultConsumer<ServerCommandSource> RESULT_REPORTER = new ResultConsumer<ServerCommandSource>(){

        @Override
        public void accept(ServerCommandSource serverCommandSource, Identifier identifier, int i) {
            serverCommandSource.sendFeedback(() -> Text.translatable("commands.function.result", Text.of(identifier), i), true);
        }
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("with");
        for (final DataCommand.ObjectType objectType : DataCommand.SOURCE_OBJECT_TYPES) {
            objectType.addArgumentsToBuilder(literalArgumentBuilder, builder -> ((ArgumentBuilder)builder.executes(new Command(){

                @Override
                protected NbtCompound getArguments(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                    return objectType.getObject(context).getNbt();
                }
            })).then(CommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes(new Command(){

                @Override
                protected NbtCompound getArguments(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                    return FunctionCommand.getArgument(NbtPathArgumentType.getNbtPath(context, "path"), objectType.getObject(context));
                }
            })));
        }
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("function").requires(source -> source.hasPermissionLevel(2))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("name", CommandFunctionArgumentType.commandFunction()).suggests(SUGGESTION_PROVIDER).executes(new Command(){

            @Override
            @Nullable
            protected NbtCompound getArguments(CommandContext<ServerCommandSource> context) {
                return null;
            }
        })).then(CommandManager.argument("arguments", NbtCompoundArgumentType.nbtCompound()).executes(new Command(){

            @Override
            protected NbtCompound getArguments(CommandContext<ServerCommandSource> context) {
                return NbtCompoundArgumentType.getNbtCompound(context, "arguments");
            }
        }))).then(literalArgumentBuilder)));
    }

    static NbtCompound getArgument(NbtPathArgumentType.NbtPath path, DataCommandObject object) throws CommandSyntaxException {
        NbtElement nbtElement = DataCommand.getNbt(path, object);
        if (nbtElement instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)nbtElement;
            return nbtCompound;
        }
        throw ARGUMENT_NOT_COMPOUND_EXCEPTION.create(nbtElement.getNbtType().getCrashReportName());
    }

    public static ServerCommandSource createFunctionCommandSource(ServerCommandSource source) {
        return source.withSilent().withMaxLevel(2);
    }

    public static <T extends AbstractServerCommandSource<T>> void enqueueAction(Collection<CommandFunction<T>> commandFunctions, @Nullable NbtCompound args, T parentSource, T functionSource, ExecutionControl<T> control, ResultConsumer<T> resultConsumer, ExecutionFlags flags) throws CommandSyntaxException {
        if (flags.isInsideReturnRun()) {
            FunctionCommand.enqueueInReturnRun(commandFunctions, args, parentSource, functionSource, control, resultConsumer);
        } else {
            FunctionCommand.enqueueOutsideReturnRun(commandFunctions, args, parentSource, functionSource, control, resultConsumer);
        }
    }

    private static <T extends AbstractServerCommandSource<T>> void enqueueFunction(@Nullable NbtCompound args, ExecutionControl<T> control, CommandDispatcher<T> dispatcher, T source, CommandFunction<T> function, Identifier id, ReturnValueConsumer returnValueConsumer, boolean propagateReturn) throws CommandSyntaxException {
        try {
            Procedure<T> procedure = function.withMacroReplaced(args, dispatcher);
            control.enqueueAction(new CommandFunctionAction<T>(procedure, returnValueConsumer, propagateReturn).bind(source));
        } catch (MacroException macroException) {
            throw INSTANTIATION_FAILURE_EXCEPTION.create(id, macroException.getMessage());
        }
    }

    private static <T extends AbstractServerCommandSource<T>> ReturnValueConsumer wrapReturnValueConsumer(T flags, ResultConsumer<T> resultConsumer, Identifier id, ReturnValueConsumer wrapped) {
        if (flags.isSilent()) {
            return wrapped;
        }
        return (successful, returnValue) -> {
            resultConsumer.accept(flags, id, returnValue);
            wrapped.onResult(successful, returnValue);
        };
    }

    private static <T extends AbstractServerCommandSource<T>> void enqueueInReturnRun(Collection<CommandFunction<T>> functions, @Nullable NbtCompound args, T parentSource, T functionSource, ExecutionControl<T> control, ResultConsumer<T> resultConsumer) throws CommandSyntaxException {
        CommandDispatcher<T> commandDispatcher = parentSource.getDispatcher();
        T abstractServerCommandSource = functionSource.withDummyReturnValueConsumer();
        ReturnValueConsumer returnValueConsumer = ReturnValueConsumer.chain(parentSource.getReturnValueConsumer(), control.getFrame().returnValueConsumer());
        for (CommandFunction<T> commandFunction : functions) {
            Identifier identifier = commandFunction.id();
            ReturnValueConsumer returnValueConsumer2 = FunctionCommand.wrapReturnValueConsumer(parentSource, resultConsumer, identifier, returnValueConsumer);
            FunctionCommand.enqueueFunction(args, control, commandDispatcher, abstractServerCommandSource, commandFunction, identifier, returnValueConsumer2, true);
        }
        control.enqueueAction(FallthroughCommandAction.getInstance());
    }

    private static <T extends AbstractServerCommandSource<T>> void enqueueOutsideReturnRun(Collection<CommandFunction<T>> functions, @Nullable NbtCompound args, T parentSource, T functionSource, ExecutionControl<T> control, ResultConsumer<T> resultConsumer) throws CommandSyntaxException {
        CommandDispatcher<T> commandDispatcher = parentSource.getDispatcher();
        T abstractServerCommandSource = functionSource.withDummyReturnValueConsumer();
        ReturnValueConsumer returnValueConsumer = parentSource.getReturnValueConsumer();
        if (functions.isEmpty()) {
            return;
        }
        if (functions.size() == 1) {
            CommandFunction<T> commandFunction = functions.iterator().next();
            Identifier identifier = commandFunction.id();
            ReturnValueConsumer returnValueConsumer2 = FunctionCommand.wrapReturnValueConsumer(parentSource, resultConsumer, identifier, returnValueConsumer);
            FunctionCommand.enqueueFunction(args, control, commandDispatcher, abstractServerCommandSource, commandFunction, identifier, returnValueConsumer2, false);
        } else if (returnValueConsumer == ReturnValueConsumer.EMPTY) {
            for (CommandFunction<T> commandFunction2 : functions) {
                Identifier identifier2 = commandFunction2.id();
                ReturnValueConsumer returnValueConsumer3 = FunctionCommand.wrapReturnValueConsumer(parentSource, resultConsumer, identifier2, returnValueConsumer);
                FunctionCommand.enqueueFunction(args, control, commandDispatcher, abstractServerCommandSource, commandFunction2, identifier2, returnValueConsumer3, false);
            }
        } else {
            class ReturnValueAdder {
                boolean successful;
                int returnValue;

                ReturnValueAdder() {
                }

                public void onSuccess(int returnValue) {
                    this.successful = true;
                    this.returnValue += returnValue;
                }
            }
            ReturnValueAdder returnValueAdder = new ReturnValueAdder();
            ReturnValueConsumer returnValueConsumer4 = (successful, returnValue) -> returnValueAdder.onSuccess(returnValue);
            for (CommandFunction<T> commandFunction3 : functions) {
                Identifier identifier3 = commandFunction3.id();
                ReturnValueConsumer returnValueConsumer5 = FunctionCommand.wrapReturnValueConsumer(parentSource, resultConsumer, identifier3, returnValueConsumer4);
                FunctionCommand.enqueueFunction(args, control, commandDispatcher, abstractServerCommandSource, commandFunction3, identifier3, returnValueConsumer5, false);
            }
            control.enqueueAction((context, frame) -> {
                if (returnValueAdder.successful) {
                    returnValueConsumer.onSuccess(returnValueAdder.returnValue);
                }
            });
        }
    }

    public static interface ResultConsumer<T> {
        public void accept(T var1, Identifier var2, int var3);
    }

    static abstract class Command
    extends ControlFlowAware.Helper<ServerCommandSource>
    implements ControlFlowAware.Command<ServerCommandSource> {
        Command() {
        }

        @Nullable
        protected abstract NbtCompound getArguments(CommandContext<ServerCommandSource> var1) throws CommandSyntaxException;

        @Override
        public void executeInner(ServerCommandSource serverCommandSource, ContextChain<ServerCommandSource> contextChain, ExecutionFlags executionFlags, ExecutionControl<ServerCommandSource> executionControl) throws CommandSyntaxException {
            CommandContext<ServerCommandSource> commandContext = contextChain.getTopContext().copyFor(serverCommandSource);
            Pair<Identifier, Collection<CommandFunction<ServerCommandSource>>> pair = CommandFunctionArgumentType.getIdentifiedFunctions(commandContext, "name");
            Collection collection = pair.getSecond();
            if (collection.isEmpty()) {
                throw NO_FUNCTIONS_EXCEPTION.create(Text.of(pair.getFirst()));
            }
            NbtCompound nbtCompound = this.getArguments(commandContext);
            ServerCommandSource serverCommandSource2 = FunctionCommand.createFunctionCommandSource(serverCommandSource);
            if (collection.size() == 1) {
                serverCommandSource.sendFeedback(() -> Text.translatable("commands.function.scheduled.single", Text.of(((CommandFunction)collection.iterator().next()).id())), true);
            } else {
                serverCommandSource.sendFeedback(() -> Text.translatable("commands.function.scheduled.multiple", Texts.join(collection.stream().map(CommandFunction::id).toList(), Text::of)), true);
            }
            FunctionCommand.enqueueAction(collection, nbtCompound, serverCommandSource, serverCommandSource2, executionControl, RESULT_REPORTER, executionFlags);
        }

        @Override
        public /* synthetic */ void executeInner(AbstractServerCommandSource source, ContextChain contextChain, ExecutionFlags flags, ExecutionControl control) throws CommandSyntaxException {
            this.executeInner((ServerCommandSource)source, (ContextChain<ServerCommandSource>)contextChain, flags, (ExecutionControl<ServerCommandSource>)control);
        }
    }
}

