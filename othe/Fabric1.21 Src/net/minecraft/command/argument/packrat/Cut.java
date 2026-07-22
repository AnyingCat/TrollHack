/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

public interface Cut {
    public static final Cut NOOP = () -> {};

    public void cut();
}

