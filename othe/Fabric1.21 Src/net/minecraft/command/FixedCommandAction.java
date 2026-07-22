/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.ExecutionFlags;
import net.minecraft.command.Frame;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.function.Tracer;

public class FixedCommandAction<T extends AbstractServerCommandSource<T>>
implements SourcedCommandAction<T> {
    private final String command;
    private final ExecutionFlags flags;
    private final CommandContext<T> context;

    public FixedCommandAction(String command, ExecutionFlags flags, CommandContext<T> context) {
        this.command = command;
        this.flags = flags;
        this.context = context;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void execute(T abstractServerCommandSource, CommandExecutionContext<T> commandExecutionContext, Frame frame) {
        commandExecutionContext.getProfiler().push(() -> "execute " + this.command);
        try {
            commandExecutionContext.decrementCommandQuota();
            int i = ContextChain.runExecutable(this.context, abstractServerCommandSource, AbstractServerCommandSource.asResultConsumer(), this.flags.isSilent());
            Tracer tracer = commandExecutionContext.getTracer();
            if (tracer != null) {
                tracer.traceCommandEnd(frame.depth(), this.command, i);
            }
        } catch (CommandSyntaxException commandSyntaxException) {
            abstractServerCommandSource.handleException(commandSyntaxException, this.flags.isSilent(), commandExecutionContext.getTracer());
        } finally {
            commandExecutionContext.getProfiler().pop();
        }
    }

    @Override
    public /* synthetic */ void execute(Object object, CommandExecutionContext commandExecutionContext, Frame frame) {
        this.execute((AbstractServerCommandSource)object, commandExecutionContext, frame);
    }
}

