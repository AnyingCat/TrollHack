/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.client;

import com.google.gson.JsonElement;
import java.util.function.Supplier;
import net.minecraft.block.Block;

/**
 * A supplier of a block state JSON definition.
 */
public interface BlockStateSupplier
extends Supplier<JsonElement> {
    public Block getBlock();
}

