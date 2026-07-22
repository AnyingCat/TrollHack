/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import java.util.List;
import net.minecraft.command.CommandAction;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.CommandQueueEntry;
import net.minecraft.command.Frame;

public class SteppedCommandAction<T, P>
implements CommandAction<T> {
    private final ActionWrapper<T, P> wrapper;
    private final List<P> actions;
    private final CommandQueueEntry<T> selfCommandQueueEntry;
    private int nextActionIndex;

    private SteppedCommandAction(ActionWrapper<T, P> wrapper, List<P> actions, Frame frame) {
        this.wrapper = wrapper;
        this.actions = actions;
        this.selfCommandQueueEntry = new CommandQueueEntry(frame, this);
    }

    @Override
    public void execute(CommandExecutionContext<T> commandExecutionContext, Frame frame) {
        P object = this.actions.get(this.nextActionIndex);
        commandExecutionContext.enqueueCommand(this.wrapper.create(frame, object));
        if (++this.nextActionIndex < this.actions.size()) {
            commandExecutionContext.enqueueCommand(this.selfCommandQueueEntry);
        }
    }

    public static <T, P> void enqueueCommands(CommandExecutionContext<T> context, Frame frame, List<P> actions, ActionWrapper<T, P> wrapper) {
        int i = actions.size();
        switch (i) {
            case 0: {
                break;
            }
            case 1: {
                context.enqueueCommand(wrapper.create(frame, actions.get(0)));
                break;
            }
            case 2: {
                context.enqueueCommand(wrapper.create(frame, actions.get(0)));
                context.enqueueCommand(wrapper.create(frame, actions.get(1)));
                break;
            }
            default: {
                context.enqueueCommand(new SteppedCommandAction<T, P>(wrapper, actions, (Frame)frame).selfCommandQueueEntry);
            }
        }
    }

    @FunctionalInterface
    public static interface ActionWrapper<T, P> {
        public CommandQueueEntry<T> create(Frame var1, P var2);
    }
}

