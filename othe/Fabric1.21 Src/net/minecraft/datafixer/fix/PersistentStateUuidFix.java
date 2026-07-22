/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.AbstractUuidFix;
import org.slf4j.Logger;

public class PersistentStateUuidFix
extends AbstractUuidFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    public PersistentStateUuidFix(Schema outputSchema) {
        super(outputSchema, TypeReferences.SAVED_DATA_RAIDS);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("SavedDataUUIDFix", this.getInputSchema().getType(this.typeReference), raidsDataTyped -> raidsDataTyped.update(DSL.remainderFinder(), raidsDataDynamic -> raidsDataDynamic.update("data", dataDynamic -> dataDynamic.update("Raids", raidsDynamic -> raidsDynamic.createList(raidsDynamic.asStream().map(raidDynamic -> raidDynamic.update("HeroesOfTheVillage", heroesOfTheVillageDynamic -> heroesOfTheVillageDynamic.createList(heroesOfTheVillageDynamic.asStream().map(heroOfTheVillageDynamic -> PersistentStateUuidFix.createArrayFromMostLeastTags(heroOfTheVillageDynamic, "UUIDMost", "UUIDLeast").orElseGet(() -> {
            LOGGER.warn("HeroesOfTheVillage contained invalid UUIDs.");
            return heroOfTheVillageDynamic;
        }))))))))));
    }
}

