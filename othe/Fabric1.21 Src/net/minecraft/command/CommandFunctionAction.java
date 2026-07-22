/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import java.util.List;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.CommandQueueEntry;
import net.minecraft.command.Frame;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.command.SteppedCommandAction;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.function.Procedure;
import net.minecraft.server.function.Tracer;

public class CommandFunctionAction<T extends AbstractServerCommandSource<T>>
implements SourcedCommandAction<T> {
    private final Procedure<T> function;
    private final ReturnValueConsumer returnValueConsumer;
    private final boolean propagateReturn;

    public CommandFunctionAction(Procedure<T> function, ReturnValueConsumer returnValueConsumer, boolean propagateReturn) {
        this.function = function;
        this.returnValueConsumer = returnValueConsumer;
        this.propagateReturn = propagateReturn;
    }

    @Override
    public void execute(T abstractServerCommandSource, CommandExecutionContext<T> commandExecutionContext, Frame frame2) {
        commandExecutionContext.decrementCommandQuota();
        List<SourcedCommandAction<T>> list = this.function.entries();
        Tracer tracer = commandExecutionContext.getTracer();
        if (tracer != null) {
            tracer.traceFunctionCall(frame2.depth(), this.function.id(), this.function.entries().size());
        }
        int i = frame2.depth() + 1;
        Frame.Control control = this.propagateReturn ? frame2.frameControl() : commandExecutionContext.getEscapeControl(i);
        Frame frame22 = new Frame(i, this.returnValueConsumer, control);
        SteppedCommandAction.enqueueCommands(commandExecutionContext, frame22, list, (frame, action) -> new CommandQueueEntry<AbstractServerCommandSource>(frame, action.bind(abstractServerCommandSource)));
    }

    @Override
    public /* synthetic */ void execute(Object object, CommandExecutionContext commandExecutionContext, Frame frame) {
        this.execute((AbstractServerCommandSource)object, commandExecutionContext, frame);
    }
}

