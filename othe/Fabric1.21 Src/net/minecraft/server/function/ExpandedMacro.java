/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.function;

import com.mojang.brigadier.CommandDispatcher;
import java.util.List;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.MacroException;
import net.minecraft.server.function.Procedure;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record ExpandedMacro<T>(Identifier id, List<SourcedCommandAction<T>> entries) implements CommandFunction<T>,
Procedure<T>
{
    @Override
    public Procedure<T> withMacroReplaced(@Nullable NbtCompound arguments, CommandDispatcher<T> dispatcher) throws MacroException {
        return this;
    }
}

