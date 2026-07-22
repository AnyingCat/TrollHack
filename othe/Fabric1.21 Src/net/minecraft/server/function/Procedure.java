/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.function;

import java.util.List;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.util.Identifier;

public interface Procedure<T> {
    public Identifier id();

    public List<SourcedCommandAction<T>> entries();
}

