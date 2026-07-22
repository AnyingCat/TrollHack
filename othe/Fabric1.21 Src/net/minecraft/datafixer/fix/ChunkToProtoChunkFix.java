/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.datafixer.TypeReferences;

public class ChunkToProtoChunkFix
extends DataFix {
    private static final int field_29881 = 16;

    public ChunkToProtoChunkFix(Schema outputSchema, boolean changestype) {
        super(outputSchema, changestype);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.writeFixAndRead("ChunkToProtoChunkFix", this.getInputSchema().getType(TypeReferences.CHUNK), this.getOutputSchema().getType(TypeReferences.CHUNK), chunkDynamic -> chunkDynamic.update("Level", ChunkToProtoChunkFix::fixLevel));
    }

    private static <T> Dynamic<T> fixLevel(Dynamic<T> levelDynamic) {
        boolean bl2;
        boolean bl = levelDynamic.get("TerrainPopulated").asBoolean(false);
        boolean bl3 = bl2 = levelDynamic.get("LightPopulated").asNumber().result().isEmpty() || levelDynamic.get("LightPopulated").asBoolean(false);
        String string = bl ? (bl2 ? "mobs_spawned" : "decorated") : "carved";
        return ChunkToProtoChunkFix.fixTileTicks(ChunkToProtoChunkFix.fixBiomes(levelDynamic)).set("Status", levelDynamic.createString(string)).set("hasLegacyStructureData", levelDynamic.createBoolean(true));
    }

    private static <T> Dynamic<T> fixBiomes(Dynamic<T> levelDynamic) {
        return levelDynamic.update("Biomes", biomesDynamic -> DataFixUtils.orElse(biomesDynamic.asByteBufferOpt().result().map(biomes -> {
            int[] is = new int[256];
            for (int i = 0; i < is.length; ++i) {
                if (i >= biomes.capacity()) continue;
                is[i] = biomes.get(i) & 0xFF;
            }
            return levelDynamic.createIntList(Arrays.stream(is));
        }), biomesDynamic));
    }

    private static <T> Dynamic<T> fixTileTicks(Dynamic<T> levelDynamic) {
        return DataFixUtils.orElse(levelDynamic.get("TileTicks").asStreamOpt().result().map(tileTicksDynamic -> {
            List list = IntStream.range(0, 16).mapToObj(sectionY -> new ShortArrayList()).collect(Collectors.toList());
            tileTicksDynamic.forEach(tickTag -> {
                int i = tickTag.get("x").asInt(0);
                int j = tickTag.get("y").asInt(0);
                int k = tickTag.get("z").asInt(0);
                short s = ChunkToProtoChunkFix.packChunkSectionPos(i, j, k);
                ((ShortList)list.get(j >> 4)).add(s);
            });
            return levelDynamic.remove("TileTicks").set("ToBeTicked", levelDynamic.createList(list.stream().map(section -> levelDynamic.createList(section.intStream().mapToObj(packedLocalPos -> levelDynamic.createShort((short)packedLocalPos))))));
        }), levelDynamic);
    }

    private static short packChunkSectionPos(int x, int y, int z) {
        return (short)(x & 0xF | (y & 0xF) << 4 | (z & 0xF) << 8);
    }
}

