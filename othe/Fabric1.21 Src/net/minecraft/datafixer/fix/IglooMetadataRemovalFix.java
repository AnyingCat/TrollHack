/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class IglooMetadataRemovalFix
extends DataFix {
    public IglooMetadataRemovalFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE);
        return this.fixTypeEverywhereTyped("IglooMetadataRemovalFix", type, structureFeatureTyped -> structureFeatureTyped.update(DSL.remainderFinder(), IglooMetadataRemovalFix::removeMetadata));
    }

    private static <T> Dynamic<T> removeMetadata(Dynamic<T> structureFeatureDynamic) {
        boolean bl = structureFeatureDynamic.get("Children").asStreamOpt().map(stream -> stream.allMatch(IglooMetadataRemovalFix::isIgloo)).result().orElse(false);
        if (bl) {
            return structureFeatureDynamic.set("id", structureFeatureDynamic.createString("Igloo")).remove("Children");
        }
        return structureFeatureDynamic.update("Children", IglooMetadataRemovalFix::removeIgloos);
    }

    private static <T> Dynamic<T> removeIgloos(Dynamic<T> structureFeatureDynamic) {
        return structureFeatureDynamic.asStreamOpt().map(stream -> stream.filter(dynamic -> !IglooMetadataRemovalFix.isIgloo(dynamic))).map(structureFeatureDynamic::createList).result().orElse(structureFeatureDynamic);
    }

    private static boolean isIgloo(Dynamic<?> structureFeatureDynamic) {
        return structureFeatureDynamic.get("id").asString("").equals("Iglu");
    }
}

