/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FieldFinder;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.StructureSeparationDataFix;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class MissingDimensionFix
extends DataFix {
    public MissingDimensionFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    protected static <A> Type<Pair<A, Dynamic<?>>> extract1(String field, Type<A> type) {
        return DSL.and(DSL.field(field, type), DSL.remainderType());
    }

    protected static <A> Type<Pair<Either<A, Unit>, Dynamic<?>>> extract1Opt(String field, Type<A> type) {
        return DSL.and(DSL.optional(DSL.field(field, type)), DSL.remainderType());
    }

    protected static <A1, A2> Type<Pair<Either<A1, Unit>, Pair<Either<A2, Unit>, Dynamic<?>>>> extract2Opt(String field1, Type<A1> type1, String field2, Type<A2> type2) {
        return DSL.and(DSL.optional(DSL.field(field1, type1)), DSL.optional(DSL.field(field2, type2)), DSL.remainderType());
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Type<Pair<String, Pair<Either<Pair<String, Dynamic<?>>, Unit>, Pair<Either<Either<String, Pair<Either<?, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>, Unit>, Dynamic<?>>>>> type = DSL.taggedChoiceType("type", DSL.string(), ImmutableMap.of("minecraft:debug", DSL.remainderType(), "minecraft:flat", MissingDimensionFix.flatGeneratorType(schema), "minecraft:noise", MissingDimensionFix.extract2Opt("biome_source", DSL.taggedChoiceType("type", DSL.string(), ImmutableMap.of("minecraft:fixed", MissingDimensionFix.extract1("biome", schema.getType(TypeReferences.BIOME)), "minecraft:multi_noise", DSL.list(MissingDimensionFix.extract1("biome", schema.getType(TypeReferences.BIOME))), "minecraft:checkerboard", MissingDimensionFix.extract1("biomes", DSL.list(schema.getType(TypeReferences.BIOME))), "minecraft:vanilla_layered", DSL.remainderType(), "minecraft:the_end", DSL.remainderType())), "settings", DSL.or(DSL.string(), MissingDimensionFix.extract2Opt("default_block", schema.getType(TypeReferences.BLOCK_NAME), "default_fluid", schema.getType(TypeReferences.BLOCK_NAME))))));
        CompoundList.CompoundListType<String, Pair<Pair<String, Pair<Either<Pair<String, Dynamic<?>>, Unit>, Pair<Either<Either<String, Pair<Either<?, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>, Unit>, Dynamic<?>>>>, Dynamic<?>>> compoundListType = DSL.compoundList(IdentifierNormalizingSchema.getIdentifierType(), MissingDimensionFix.extract1("generator", type));
        Type type2 = DSL.and(compoundListType, DSL.remainderType());
        Type<?> type3 = schema.getType(TypeReferences.WORLD_GEN_SETTINGS);
        FieldFinder fieldFinder = new FieldFinder("dimensions", type2);
        if (!type3.findFieldType("dimensions").equals(type2)) {
            throw new IllegalStateException();
        }
        OpticFinder opticFinder = compoundListType.finder();
        return this.fixTypeEverywhereTyped("MissingDimensionFix", type3, worldGenSettingsTyped -> worldGenSettingsTyped.updateTyped(fieldFinder, dimensionsTyped -> dimensionsTyped.updateTyped(opticFinder, dimensionsListTyped -> {
            if (!(dimensionsListTyped.getValue() instanceof List)) {
                throw new IllegalStateException("List exptected");
            }
            if (((List)dimensionsListTyped.getValue()).isEmpty()) {
                Dynamic<?> dynamic = worldGenSettingsTyped.get(DSL.remainderFinder());
                Dynamic<?> dynamic2 = this.method_29912(dynamic);
                return DataFixUtils.orElse(compoundListType.readTyped(dynamic2).result().map(Pair::getFirst), dimensionsListTyped);
            }
            return dimensionsListTyped;
        })));
    }

    protected static Type<? extends Pair<? extends Either<? extends Pair<? extends Either<?, Unit>, ? extends Pair<? extends Either<? extends List<? extends Pair<? extends Either<?, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>> flatGeneratorType(Schema schema) {
        return MissingDimensionFix.extract1Opt("settings", MissingDimensionFix.extract2Opt("biome", schema.getType(TypeReferences.BIOME), "layers", DSL.list(MissingDimensionFix.extract1Opt("block", schema.getType(TypeReferences.BLOCK_NAME)))));
    }

    private <T> Dynamic<T> method_29912(Dynamic<T> worldGenSettingsDynamic) {
        long l = worldGenSettingsDynamic.get("seed").asLong(0L);
        return new Dynamic(worldGenSettingsDynamic.getOps(), StructureSeparationDataFix.createDimensionSettings(worldGenSettingsDynamic, l, StructureSeparationDataFix.createDefaultOverworldGeneratorSettings(worldGenSettingsDynamic, l), false));
    }
}

