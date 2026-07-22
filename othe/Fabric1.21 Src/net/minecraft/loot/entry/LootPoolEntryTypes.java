/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.loot.entry.AlternativeEntry;
import net.minecraft.loot.entry.DynamicEntry;
import net.minecraft.loot.entry.EmptyEntry;
import net.minecraft.loot.entry.GroupEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.entry.SequenceEntry;
import net.minecraft.loot.entry.TagEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class LootPoolEntryTypes {
    public static final Codec<LootPoolEntry> CODEC = Registries.LOOT_POOL_ENTRY_TYPE.getCodec().dispatch(LootPoolEntry::getType, LootPoolEntryType::codec);
    public static final LootPoolEntryType EMPTY = LootPoolEntryTypes.register("empty", EmptyEntry.CODEC);
    public static final LootPoolEntryType ITEM = LootPoolEntryTypes.register("item", ItemEntry.CODEC);
    public static final LootPoolEntryType LOOT_TABLE = LootPoolEntryTypes.register("loot_table", LootTableEntry.CODEC);
    public static final LootPoolEntryType DYNAMIC = LootPoolEntryTypes.register("dynamic", DynamicEntry.CODEC);
    public static final LootPoolEntryType TAG = LootPoolEntryTypes.register("tag", TagEntry.CODEC);
    public static final LootPoolEntryType ALTERNATIVES = LootPoolEntryTypes.register("alternatives", AlternativeEntry.CODEC);
    public static final LootPoolEntryType SEQUENCE = LootPoolEntryTypes.register("sequence", SequenceEntry.CODEC);
    public static final LootPoolEntryType GROUP = LootPoolEntryTypes.register("group", GroupEntry.CODEC);

    private static LootPoolEntryType register(String id, MapCodec<? extends LootPoolEntry> codec) {
        return Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, Identifier.ofVanilla(id), new LootPoolEntryType(codec));
    }
}

