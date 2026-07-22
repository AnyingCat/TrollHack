/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package com.mojang.blaze3d.systems;

import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.ints.IntArrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public interface VertexSorter {
    public static final VertexSorter BY_DISTANCE = VertexSorter.byDistance(0.0f, 0.0f, 0.0f);
    public static final VertexSorter BY_Z = VertexSorter.of(vec -> -vec.z());

    public static VertexSorter byDistance(float originX, float originY, float originZ) {
        return VertexSorter.byDistance(new Vector3f(originX, originY, originZ));
    }

    public static VertexSorter byDistance(Vector3f origin) {
        return VertexSorter.of(origin::distanceSquared);
    }

    public static VertexSorter of(SortKeyMapper mapper) {
        return vec -> {
            float[] fs = new float[vec.length];
            int[] is = new int[vec.length];
            for (int i = 0; i < vec.length; ++i) {
                fs[i] = mapper.apply(vec[i]);
                is[i] = i;
            }
            IntArrays.mergeSort(is, (a, b) -> Floats.compare(fs[b], fs[a]));
            return is;
        };
    }

    public int[] sort(Vector3f[] var1);

    @Environment(value=EnvType.CLIENT)
    public static interface SortKeyMapper {
        public float apply(Vector3f var1);
    }
}

