/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard.number;

import net.minecraft.scoreboard.number.NumberFormatType;
import net.minecraft.text.MutableText;

public interface NumberFormat {
    public MutableText format(int var1);

    public NumberFormatType<? extends NumberFormat> getType();
}

