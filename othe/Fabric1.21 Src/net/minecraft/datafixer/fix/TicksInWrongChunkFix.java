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
import java.util.List;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class TicksInWrongChunkFix
extends DataFix {
    public TicksInWrongChunkFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.CHUNK);
        OpticFinder<?> opticFinder = type.findField("block_ticks");
        return this.fixTypeEverywhereTyped("Handle ticks saved in the wrong chunk", type, chunkTyped -> {
            Optional optional = chunkTyped.getOptionalTyped(opticFinder);
            Optional optional2 = optional.isPresent() ? optional.get().write().result() : Optional.empty();
            return chunkTyped.update(DSL.remainderFinder(), chunkTag -> {
                int i = chunkTag.get("xPos").asInt(0);
                int j = chunkTag.get("zPos").asInt(0);
                Optional optional2 = chunkTag.get("fluid_ticks").get().result();
                chunkTag = TicksInWrongChunkFix.putNeighborTicks(chunkTag, i, j, optional2, "neighbor_block_ticks");
                chunkTag = TicksInWrongChunkFix.putNeighborTicks(chunkTag, i, j, optional2, "neighbor_fluid_ticks");
                return chunkTag;
            });
        });
    }

    private static Dynamic<?> putNeighborTicks(Dynamic<?> chunkTag, int chunkX, int chunkZ, Optional<? extends Dynamic<?>> ticks, String upgradeDataKey) {
        List<Dynamic> list;
        if (ticks.isPresent() && !(list = ticks.get().asStream().filter(tickDynamic -> {
            int k = tickDynamic.get("x").asInt(0);
            int l = tickDynamic.get("z").asInt(0);
            int m = Math.abs(chunkX - (k >> 4));
            int n = Math.abs(chunkZ - (l >> 4));
            return (m != 0 || n != 0) && m <= 1 && n <= 1;
        }).toList()).isEmpty()) {
            chunkTag = chunkTag.set("UpgradeData", chunkTag.get("UpgradeData").orElseEmptyMap().set(upgradeDataKey, chunkTag.createList(list.stream())));
        }
        return chunkTag;
    }
}

