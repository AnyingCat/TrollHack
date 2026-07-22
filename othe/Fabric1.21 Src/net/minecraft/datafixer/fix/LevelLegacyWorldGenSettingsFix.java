/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class LevelLegacyWorldGenSettingsFix
extends DataFix {
    private static final String WORLD_GEN_SETTINGS_KEY = "WorldGenSettings";
    private static final List<String> SETTINGS_TO_FIX = List.of("RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest");

    public LevelLegacyWorldGenSettingsFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LevelLegacyWorldGenSettingsFix", this.getInputSchema().getType(TypeReferences.LEVEL), typed -> typed.update(DSL.remainderFinder(), data -> {
            Dynamic dynamic = data.get(WORLD_GEN_SETTINGS_KEY).orElseEmptyMap();
            for (String string : SETTINGS_TO_FIX) {
                Optional optional = data.get(string).result();
                if (!optional.isPresent()) continue;
                data = data.remove(string);
                dynamic = dynamic.set(string, optional.get());
            }
            return data.set(WORLD_GEN_SETTINGS_KEY, dynamic);
        }));
    }
}

