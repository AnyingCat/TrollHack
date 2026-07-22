/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.function;

import net.minecraft.text.Text;

public class MacroException
extends Exception {
    private final Text message;

    public MacroException(Text message) {
        super(message.getString());
        this.message = message;
    }

    public Text getMessage() {
        return this.message;
    }
}

