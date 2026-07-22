/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.function;

import net.minecraft.util.Identifier;

/**
 * A tree-visitor-like tracer, useful for gaining insights on function execution.
 */
public interface Tracer
extends AutoCloseable {
    public void traceCommandStart(int var1, String var2);

    public void traceCommandEnd(int var1, String var2, int var3);

    public void traceError(String var1);

    public void traceFunctionCall(int var1, Identifier var2, int var3);

    @Override
    public void close();
}

