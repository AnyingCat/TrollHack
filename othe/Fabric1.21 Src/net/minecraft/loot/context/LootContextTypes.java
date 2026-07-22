/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.context;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.util.Identifier;

public class LootContextTypes {
    private static final BiMap<Identifier, LootContextType> MAP = HashBiMap.create();
    public static final Codec<LootContextType> CODEC = Identifier.CODEC.comapFlatMap(id -> Optional.ofNullable((LootContextType)MAP.get(id)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "No parameter set exists with id: '" + String.valueOf(id) + "'")), MAP.inverse()::get);
    public static final LootContextType EMPTY = LootContextTypes.register("empty", builder -> {});
    public static final LootContextType CHEST = LootContextTypes.register("chest", builder -> builder.require(LootContextParameters.ORIGIN).allow(LootContextParameters.THIS_ENTITY));
    public static final LootContextType COMMAND = LootContextTypes.register("command", builder -> builder.require(LootContextParameters.ORIGIN).allow(LootContextParameters.THIS_ENTITY));
    public static final LootContextType SELECTOR = LootContextTypes.register("selector", builder -> builder.require(LootContextParameters.ORIGIN).require(LootContextParameters.THIS_ENTITY));
    public static final LootContextType FISHING = LootContextTypes.register("fishing", builder -> builder.require(LootContextParameters.ORIGIN).require(LootContextParameters.TOOL).allow(LootContextParameters.THIS_ENTITY));
    public static final LootContextType ENTITY = LootContextTypes.register("entity", builder -> builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ORIGIN).require(LootContextParameters.DAMAGE_SOURCE).allow(LootContextParameters.ATTACKING_ENTITY).allow(LootContextParameters.DIRECT_ATTACKING_ENTITY).allow(LootContextParameters.LAST_DAMAGE_PLAYER));
    public static final LootContextType EQUIPMENT = LootContextTypes.register("equipment", builder -> builder.require(LootContextParameters.ORIGIN).require(LootContextParameters.THIS_ENTITY));
    public static final LootContextType ARCHAEOLOGY = LootContextTypes.register("archaeology", builder -> builder.require(LootContextParameters.ORIGIN).allow(LootContextParameters.THIS_ENTITY));
    public static final LootContextType GIFT = LootContextTypes.register("gift", builder -> builder.require(LootContextParameters.ORIGIN).require(LootContextParameters.THIS_ENTITY));
    public static final LootContextType BARTER = LootContextTypes.register("barter", builder -> builder.require(LootContextParameters.THIS_ENTITY));
    public static final LootContextType VAULT = LootContextTypes.register("vault", builder -> builder.require(LootContextParameters.ORIGIN).allow(LootContextParameters.THIS_ENTITY));
    public static final LootContextType ADVANCEMENT_REWARD = LootContextTypes.register("advancement_reward", builder -> builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ORIGIN));
    public static final LootContextType ADVANCEMENT_ENTITY = LootContextTypes.register("advancement_entity", builder -> builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ORIGIN));
    public static final LootContextType ADVANCEMENT_LOCATION = LootContextTypes.register("advancement_location", builder -> builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ORIGIN).require(LootContextParameters.TOOL).require(LootContextParameters.BLOCK_STATE));
    public static final LootContextType BLOCK_USE = LootContextTypes.register("block_use", builder -> builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ORIGIN).require(LootContextParameters.BLOCK_STATE));
    public static final LootContextType GENERIC = LootContextTypes.register("generic", builder -> builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.LAST_DAMAGE_PLAYER).require(LootContextParameters.DAMAGE_SOURCE).require(LootContextParameters.ATTACKING_ENTITY).require(LootContextParameters.DIRECT_ATTACKING_ENTITY).require(LootContextParameters.ORIGIN).require(LootContextParameters.BLOCK_STATE).require(LootContextParameters.BLOCK_ENTITY).require(LootContextParameters.TOOL).require(LootContextParameters.EXPLOSION_RADIUS));
    public static final LootContextType BLOCK = LootContextTypes.register("block", builder -> builder.require(LootContextParameters.BLOCK_STATE).require(LootContextParameters.ORIGIN).require(LootContextParameters.TOOL).allow(LootContextParameters.THIS_ENTITY).allow(LootContextParameters.BLOCK_ENTITY).allow(LootContextParameters.EXPLOSION_RADIUS));
    public static final LootContextType SHEARING = LootContextTypes.register("shearing", builder -> builder.require(LootContextParameters.ORIGIN).allow(LootContextParameters.THIS_ENTITY));
    public static final LootContextType ENCHANTED_DAMAGE = LootContextTypes.register("enchanted_damage", builder -> builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ENCHANTMENT_LEVEL).require(LootContextParameters.ORIGIN).require(LootContextParameters.DAMAGE_SOURCE).allow(LootContextParameters.DIRECT_ATTACKING_ENTITY).allow(LootContextParameters.ATTACKING_ENTITY));
    public static final LootContextType ENCHANTED_ITEM = LootContextTypes.register("enchanted_item", builder -> builder.require(LootContextParameters.TOOL).require(LootContextParameters.ENCHANTMENT_LEVEL));
    public static final LootContextType ENCHANTED_LOCATION = LootContextTypes.register("enchanted_location", builder -> builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ENCHANTMENT_LEVEL).require(LootContextParameters.ORIGIN).require(LootContextParameters.ENCHANTMENT_ACTIVE));
    public static final LootContextType ENCHANTED_ENTITY = LootContextTypes.register("enchanted_entity", builder -> builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ENCHANTMENT_LEVEL).require(LootContextParameters.ORIGIN));
    public static final LootContextType HIT_BLOCK = LootContextTypes.register("hit_block", builder -> builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ENCHANTMENT_LEVEL).require(LootContextParameters.ORIGIN).require(LootContextParameters.BLOCK_STATE));

    private static LootContextType register(String name, Consumer<LootContextType.Builder> type) {
        LootContextType.Builder builder = new LootContextType.Builder();
        type.accept(builder);
        LootContextType lootContextType = builder.build();
        Identifier identifier = Identifier.ofVanilla(name);
        LootContextType lootContextType2 = MAP.put(identifier, lootContextType);
        if (lootContextType2 != null) {
            throw new IllegalStateException("Loot table parameter set " + String.valueOf(identifier) + " is already registered");
        }
        return lootContextType;
    }
}

