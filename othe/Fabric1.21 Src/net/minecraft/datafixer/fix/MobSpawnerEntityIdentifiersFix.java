/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class MobSpawnerEntityIdentifiersFix
extends DataFix {
    public MobSpawnerEntityIdentifiersFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    private Dynamic<?> fixSpawner(Dynamic<?> spawnerDynamic) {
        Optional<Stream<Dynamic<?>>> optional2;
        if (!"MobSpawner".equals(spawnerDynamic.get("id").asString(""))) {
            return spawnerDynamic;
        }
        Optional<String> optional = spawnerDynamic.get("EntityId").asString().result();
        if (optional.isPresent()) {
            Dynamic dynamic = DataFixUtils.orElse(spawnerDynamic.get("SpawnData").result(), spawnerDynamic.emptyMap());
            dynamic = dynamic.set("id", dynamic.createString(optional.get().isEmpty() ? "Pig" : optional.get()));
            spawnerDynamic = spawnerDynamic.set("SpawnData", dynamic);
            spawnerDynamic = spawnerDynamic.remove("EntityId");
        }
        if ((optional2 = spawnerDynamic.get("SpawnPotentials").asStreamOpt().result()).isPresent()) {
            spawnerDynamic = spawnerDynamic.set("SpawnPotentials", spawnerDynamic.createList(optional2.get().map(spawnPotentialsDynamic -> {
                Optional<String> optional = spawnPotentialsDynamic.get("Type").asString().result();
                if (optional.isPresent()) {
                    Dynamic dynamic = DataFixUtils.orElse(spawnPotentialsDynamic.get("Properties").result(), spawnPotentialsDynamic.emptyMap()).set("id", spawnPotentialsDynamic.createString(optional.get()));
                    return spawnPotentialsDynamic.set("Entity", dynamic).remove("Type").remove("Properties");
                }
                return spawnPotentialsDynamic;
            })));
        }
        return spawnerDynamic;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getOutputSchema().getType(TypeReferences.UNTAGGED_SPAWNER);
        return this.fixTypeEverywhereTyped("MobSpawnerEntityIdentifiersFix", this.getInputSchema().getType(TypeReferences.UNTAGGED_SPAWNER), type, (Typed<?> untaggedSpawnerTyped) -> {
            Dynamic dynamic = untaggedSpawnerTyped.get(DSL.remainderFinder());
            DataResult dataResult = type.readTyped(this.fixSpawner(dynamic = dynamic.set("id", dynamic.createString("MobSpawner"))));
            if (dataResult.result().isEmpty()) {
                return untaggedSpawnerTyped;
            }
            return dataResult.result().get().getFirst();
        });
    }
}

