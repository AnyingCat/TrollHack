/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

public record ExecutionFlags(byte flags) {
    public static final ExecutionFlags NONE = new ExecutionFlags(0);
    private static final byte SILENT = 1;
    private static final byte INSIDE_RETURN_RUN = 2;

    private ExecutionFlags set(byte flag) {
        int i = this.flags | flag;
        return i != this.flags ? new ExecutionFlags((byte)i) : this;
    }

    public boolean isSilent() {
        return (this.flags & 1) != 0;
    }

    public ExecutionFlags setSilent() {
        return this.set((byte)1);
    }

    public boolean isInsideReturnRun() {
        return (this.flags & 2) != 0;
    }

    public ExecutionFlags setInsideReturnRun() {
        return this.set((byte)2);
    }
}

