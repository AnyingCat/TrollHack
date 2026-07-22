/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import net.minecraft.command.CommandAction;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.Frame;

public record CommandQueueEntry<T>(Frame frame, CommandAction<T> action) {
    public void execute(CommandExecutionContext<T> context) {
        this.action.execute(context, this.frame);
    }
}

