/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import net.minecraft.command.ExecutionControl;
import net.minecraft.command.ExecutionFlags;

public interface Forkable<T> {
    public void execute(T var1, List<T> var2, ContextChain<T> var3, ExecutionFlags var4, ExecutionControl<T> var5);

    public static interface RedirectModifier<T>
    extends com.mojang.brigadier.RedirectModifier<T>,
    Forkable<T> {
        @Override
        default public Collection<T> apply(CommandContext<T> context) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }
}

