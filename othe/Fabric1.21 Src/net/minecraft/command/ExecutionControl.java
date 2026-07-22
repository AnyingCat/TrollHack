/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import net.minecraft.command.CommandAction;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.CommandQueueEntry;
import net.minecraft.command.Frame;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.function.Tracer;
import org.jetbrains.annotations.Nullable;

public interface ExecutionControl<T> {
    public void enqueueAction(CommandAction<T> var1);

    public void setTracer(@Nullable Tracer var1);

    @Nullable
    public Tracer getTracer();

    public Frame getFrame();

    public static <T extends AbstractServerCommandSource<T>> ExecutionControl<T> of(final CommandExecutionContext<T> context, final Frame frame) {
        return new ExecutionControl<T>(){

            @Override
            public void enqueueAction(CommandAction<T> action) {
                context.enqueueCommand(new CommandQueueEntry(frame, action));
            }

            @Override
            public void setTracer(@Nullable Tracer tracer) {
                context.setTracer(tracer);
            }

            @Override
            @Nullable
            public Tracer getTracer() {
                return context.getTracer();
            }

            @Override
            public Frame getFrame() {
                return frame;
            }
        };
    }
}

