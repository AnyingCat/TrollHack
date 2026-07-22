/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class HeightmapRenamingFix
extends DataFix {
    public HeightmapRenamingFix(Schema outputSchema, boolean changesTyped) {
        super(outputSchema, changesTyped);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.CHUNK);
        OpticFinder<?> opticFinder = type.findField("Level");
        return this.fixTypeEverywhereTyped("HeightmapRenamingFix", type, chunkTyped -> chunkTyped.updateTyped(opticFinder, levelTyped -> levelTyped.update(DSL.remainderFinder(), this::renameHeightmapTags)));
    }

    private Dynamic<?> renameHeightmapTags(Dynamic<?> levelDynamic) {
        Optional<Dynamic<?>> optional5;
        Optional<Dynamic<?>> optional4;
        Optional<Dynamic<?>> optional3;
        Optional<Dynamic<?>> optional = levelDynamic.get("Heightmaps").result();
        if (optional.isEmpty()) {
            return levelDynamic;
        }
        Dynamic<?> dynamic = optional.get();
        Optional<Dynamic<?>> optional2 = dynamic.get("LIQUID").result();
        if (optional2.isPresent()) {
            dynamic = dynamic.remove("LIQUID");
            dynamic = dynamic.set("WORLD_SURFACE_WG", optional2.get());
        }
        if ((optional3 = dynamic.get("SOLID").result()).isPresent()) {
            dynamic = dynamic.remove("SOLID");
            dynamic = dynamic.set("OCEAN_FLOOR_WG", optional3.get());
            dynamic = dynamic.set("OCEAN_FLOOR", optional3.get());
        }
        if ((optional4 = dynamic.get("LIGHT").result()).isPresent()) {
            dynamic = dynamic.remove("LIGHT");
            dynamic = dynamic.set("LIGHT_BLOCKING", optional4.get());
        }
        if ((optional5 = dynamic.get("RAIN").result()).isPresent()) {
            dynamic = dynamic.remove("RAIN");
            dynamic = dynamic.set("MOTION_BLOCKING", optional5.get());
            dynamic = dynamic.set("MOTION_BLOCKING_NO_LEAVES", optional5.get());
        }
        return levelDynamic.set("Heightmaps", dynamic);
    }
}

