/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class BlockKeys {
    public static final RegistryKey<Block> PUMPKIN = BlockKeys.of("pumpkin");
    public static final RegistryKey<Block> PUMPKIN_STEM = BlockKeys.of("pumpkin_stem");
    public static final RegistryKey<Block> ATTACHED_PUMPKIN_STEM = BlockKeys.of("attached_pumpkin_stem");
    public static final RegistryKey<Block> MELON = BlockKeys.of("melon");
    public static final RegistryKey<Block> MELON_STEM = BlockKeys.of("melon_stem");
    public static final RegistryKey<Block> ATTACHED_MELON_STEM = BlockKeys.of("attached_melon_stem");

    private static RegistryKey<Block> of(String id) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.ofVanilla(id));
    }
}

