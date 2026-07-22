/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer;

import com.mojang.datafixers.DSL;

/**
 * Represents all the type references Minecraft's datafixer can fix.
 */
public class TypeReferences {
    public static final DSL.TypeReference LEVEL = TypeReferences.create("level");
    /**
     * A type reference which refers to a player.
     */
    public static final DSL.TypeReference PLAYER = TypeReferences.create("player");
    /**
     * A type reference which refers to a chunk.
     */
    public static final DSL.TypeReference CHUNK = TypeReferences.create("chunk");
    /**
     * A type reference which refers to the saved creative hotbars.
     * 
     * <p>This type reference is only used on the client.
     */
    public static final DSL.TypeReference HOTBAR = TypeReferences.create("hotbar");
    /**
     * A type reference which refers to client game options.
     */
    public static final DSL.TypeReference OPTIONS = TypeReferences.create("options");
    public static final DSL.TypeReference STRUCTURE = TypeReferences.create("structure");
    public static final DSL.TypeReference STATS = TypeReferences.create("stats");
    public static final DSL.TypeReference SAVED_DATA_COMMAND_STORAGE = TypeReferences.create("saved_data/command_storage");
    public static final DSL.TypeReference SAVED_DATA_CHUNKS = TypeReferences.create("saved_data/chunks");
    public static final DSL.TypeReference SAVED_DATA_MAP_DATA = TypeReferences.create("saved_data/map_data");
    public static final DSL.TypeReference SAVED_DATA_IDCOUNTS = TypeReferences.create("saved_data/idcounts");
    public static final DSL.TypeReference SAVED_DATA_RAIDS = TypeReferences.create("saved_data/raids");
    public static final DSL.TypeReference SAVED_DATA_RANDOM_SEQUENCES = TypeReferences.create("saved_data/random_sequences");
    public static final DSL.TypeReference SAVED_DATA_STRUCTURE_FEATURE_INDICES = TypeReferences.create("saved_data/structure_feature_indices");
    public static final DSL.TypeReference SAVED_DATA_SCOREBOARD = TypeReferences.create("saved_data/scoreboard");
    public static final DSL.TypeReference ADVANCEMENTS = TypeReferences.create("advancements");
    /**
     * A type reference which refers to the point of interest data in a chunk.
     */
    public static final DSL.TypeReference POI_CHUNK = TypeReferences.create("poi_chunk");
    /**
     * A type reference which refers to the entity data in a chunk.
     */
    public static final DSL.TypeReference ENTITY_CHUNK = TypeReferences.create("entity_chunk");
    /**
     * A type reference which refers to a block entity.
     */
    public static final DSL.TypeReference BLOCK_ENTITY = TypeReferences.create("block_entity");
    /**
     * A type reference which refers to an item stack.
     */
    public static final DSL.TypeReference ITEM_STACK = TypeReferences.create("item_stack");
    /**
     * A type reference which refers to a block state.
     */
    public static final DSL.TypeReference BLOCK_STATE = TypeReferences.create("block_state");
    public static final DSL.TypeReference FLAT_BLOCK_STATE = TypeReferences.create("flat_block_state");
    public static final DSL.TypeReference DATA_COMPONENTS = TypeReferences.create("data_components");
    public static final DSL.TypeReference VILLAGER_TRADE = TypeReferences.create("villager_trade");
    public static final DSL.TypeReference PARTICLE = TypeReferences.create("particle");
    /**
     * A type reference which refers to an entity's identifier.
     */
    public static final DSL.TypeReference ENTITY_NAME = TypeReferences.create("entity_name");
    /**
     * A type reference which refers to an entity tree.
     * 
     * <p>An entity tree contains the passengers of an entity and their passengers.
     */
    public static final DSL.TypeReference ENTITY_TREE = TypeReferences.create("entity_tree");
    /**
     * A type reference which refers to a type of entity.
     */
    public static final DSL.TypeReference ENTITY = TypeReferences.create("entity");
    /**
     * A type reference which refers to a block's identifier.
     */
    public static final DSL.TypeReference BLOCK_NAME = TypeReferences.create("block_name");
    /**
     * A type reference which refers to an item's identifier.
     */
    public static final DSL.TypeReference ITEM_NAME = TypeReferences.create("item_name");
    public static final DSL.TypeReference GAME_EVENT_NAME = TypeReferences.create("game_event_name");
    public static final DSL.TypeReference UNTAGGED_SPAWNER = TypeReferences.create("untagged_spawner");
    public static final DSL.TypeReference STRUCTURE_FEATURE = TypeReferences.create("structure_feature");
    public static final DSL.TypeReference OBJECTIVE = TypeReferences.create("objective");
    public static final DSL.TypeReference TEAM = TypeReferences.create("team");
    public static final DSL.TypeReference RECIPE = TypeReferences.create("recipe");
    /**
     * A type reference which refers to a biome.
     */
    public static final DSL.TypeReference BIOME = TypeReferences.create("biome");
    public static final DSL.TypeReference MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST = TypeReferences.create("multi_noise_biome_source_parameter_list");
    /**
     * A type reference which refers to world gen settings.
     */
    public static final DSL.TypeReference WORLD_GEN_SETTINGS = TypeReferences.create("world_gen_settings");

    public static DSL.TypeReference create(final String typeName) {
        return new DSL.TypeReference(){

            @Override
            public String typeName() {
                return typeName;
            }

            public String toString() {
                return "@" + typeName;
            }
        };
    }
}

