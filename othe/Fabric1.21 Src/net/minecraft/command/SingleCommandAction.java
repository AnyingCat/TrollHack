/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.command.CommandAction;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.CommandQueueEntry;
import net.minecraft.command.ControlFlowAware;
import net.minecraft.command.ExecutionControl;
import net.minecraft.command.ExecutionFlags;
import net.minecraft.command.FallthroughCommandAction;
import net.minecraft.command.FixedCommandAction;
import net.minecraft.command.Forkable;
import net.minecraft.command.Frame;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.command.SteppedCommandAction;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.function.Tracer;
import net.minecraft.text.Text;

public class SingleCommandAction<T extends AbstractServerCommandSource<T>> {
    @VisibleForTesting
    public static final DynamicCommandExceptionType FORK_LIMIT_EXCEPTION = new DynamicCommandExceptionType(count -> Text.stringifiedTranslatable("command.forkLimit", count));
    private final String command;
    private final ContextChain<T> contextChain;

    public SingleCommandAction(String command, ContextChain<T> contextChain) {
        this.command = command;
        this.contextChain = contextChain;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void execute(T baseSource, List<T> sources, CommandExecutionContext<T> context2, Frame frame2, ExecutionFlags flags) {
        ContextChain<T> contextChain = this.contextChain;
        ExecutionFlags executionFlags = flags;
        List<Object> list = sources;
        if (contextChain.getStage() != ContextChain.Stage.EXECUTE) {
            context2.getProfiler().push(() -> "prepare " + this.command);
            try {
                int i = context2.getForkLimit();
                while (contextChain.getStage() != ContextChain.Stage.EXECUTE) {
                    RedirectModifier<T> redirectModifier;
                    CommandContext<T> commandContext = contextChain.getTopContext();
                    if (commandContext.isForked()) {
                        executionFlags = executionFlags.setSilent();
                    }
                    if ((redirectModifier = commandContext.getRedirectModifier()) instanceof Forkable) {
                        Forkable forkable = (Forkable)((Object)redirectModifier);
                        forkable.execute(baseSource, list, contextChain, executionFlags, ExecutionControl.of(context2, frame2));
                        return;
                    }
                    if (redirectModifier != null) {
                        context2.decrementCommandQuota();
                        boolean bl = executionFlags.isSilent();
                        ObjectArrayList list2 = new ObjectArrayList();
                        for (AbstractServerCommandSource abstractServerCommandSource : list) {
                            Collection<AbstractServerCommandSource> collection;
                            block21: {
                                try {
                                    collection = ContextChain.runModifier(commandContext, abstractServerCommandSource, (context, successful, returnValue) -> {}, bl);
                                    if (list2.size() + collection.size() < i) break block21;
                                    baseSource.handleException(FORK_LIMIT_EXCEPTION.create(i), bl, context2.getTracer());
                                    return;
                                } catch (CommandSyntaxException commandSyntaxException) {
                                    abstractServerCommandSource.handleException(commandSyntaxException, bl, context2.getTracer());
                                    if (bl) continue;
                                    context2.getProfiler().pop();
                                    return;
                                }
                            }
                            list2.addAll(collection);
                        }
                        list = list2;
                    }
                    contextChain = contextChain.nextStage();
                }
            } finally {
                context2.getProfiler().pop();
            }
        }
        if (list.isEmpty()) {
            if (executionFlags.isInsideReturnRun()) {
                context2.enqueueCommand(new CommandQueueEntry(frame2, FallthroughCommandAction.getInstance()));
            }
            return;
        }
        CommandContext<T> commandContext2 = contextChain.getTopContext();
        Command<T> command = commandContext2.getCommand();
        if (command instanceof ControlFlowAware) {
            ControlFlowAware controlFlowAware = (ControlFlowAware)((Object)command);
            ExecutionControl executionControl = ExecutionControl.of(context2, frame2);
            for (AbstractServerCommandSource abstractServerCommandSource : list) {
                controlFlowAware.execute(abstractServerCommandSource, contextChain, executionFlags, executionControl);
            }
        } else {
            if (executionFlags.isInsideReturnRun()) {
                AbstractServerCommandSource abstractServerCommandSource3 = (AbstractServerCommandSource)list.get(0);
                abstractServerCommandSource3 = abstractServerCommandSource3.withReturnValueConsumer(ReturnValueConsumer.chain(abstractServerCommandSource3.getReturnValueConsumer(), frame2.returnValueConsumer()));
                list = List.of(abstractServerCommandSource3);
            }
            FixedCommandAction<T> fixedCommandAction = new FixedCommandAction<T>(this.command, executionFlags, commandContext2);
            SteppedCommandAction.enqueueCommands(context2, frame2, list, (frame, source) -> new CommandQueueEntry<AbstractServerCommandSource>(frame, fixedCommandAction.bind(source)));
        }
    }

    protected void traceCommandStart(CommandExecutionContext<T> context, Frame frame) {
        Tracer tracer = context.getTracer();
        if (tracer != null) {
            tracer.traceCommandStart(frame.depth(), this.command);
        }
    }

    public String toString() {
        return this.command;
    }

    public static class SingleSource<T extends AbstractServerCommandSource<T>>
    extends SingleCommandAction<T>
    implements CommandAction<T> {
        private final T source;

        public SingleSource(String command, ContextChain<T> contextChain, T source) {
            super(command, contextChain);
            this.source = source;
        }

        @Override
        public void execute(CommandExecutionContext<T> commandExecutionContext, Frame frame) {
            this.traceCommandStart(commandExecutionContext, frame);
            this.execute(this.source, List.of(this.source), commandExecutionContext, frame, ExecutionFlags.NONE);
        }
    }

    public static class MultiSource<T extends AbstractServerCommandSource<T>>
    extends SingleCommandAction<T>
    implements CommandAction<T> {
        private final ExecutionFlags flags;
        private final T baseSource;
        private final List<T> sources;

        public MultiSource(String command, ContextChain<T> contextChain, ExecutionFlags flags, T baseSource, List<T> sources) {
            super(command, contextChain);
            this.baseSource = baseSource;
            this.sources = sources;
            this.flags = flags;
        }

        @Override
        public void execute(CommandExecutionContext<T> commandExecutionContext, Frame frame) {
            this.execute(this.baseSource, this.sources, commandExecutionContext, frame, this.flags);
        }
    }

    public static class Sourced<T extends AbstractServerCommandSource<T>>
    extends SingleCommandAction<T>
    implements SourcedCommandAction<T> {
        public Sourced(String string, ContextChain<T> contextChain) {
            super(string, contextChain);
        }

        @Override
        public void execute(T abstractServerCommandSource, CommandExecutionContext<T> commandExecutionContext, Frame frame) {
            this.traceCommandStart(commandExecutionContext, frame);
            this.execute(abstractServerCommandSource, List.of(abstractServerCommandSource), commandExecutionContext, frame, ExecutionFlags.NONE);
        }

        @Override
        public /* synthetic */ void execute(Object object, CommandExecutionContext commandExecutionContext, Frame frame) {
            this.execute((AbstractServerCommandSource)object, commandExecutionContext, frame);
        }
    }
}

