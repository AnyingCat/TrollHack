/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.font;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.function.IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GlyphContainer<T> {
    private static final int ROW_SHIFT = 8;
    private static final int ENTRIES_PER_ROW = 256;
    private static final int LAST_ENTRY_NUM_IN_ROW = 255;
    private static final int LAST_ROW_NUM = 4351;
    private static final int NUM_ROWS = 4352;
    private final T[] defaultRow;
    private final T[][] rows;
    private final IntFunction<T[]> makeRow;

    public GlyphContainer(IntFunction<T[]> makeRow, IntFunction<T[][]> makeScroll) {
        this.defaultRow = makeRow.apply(256);
        this.rows = makeScroll.apply(4352);
        Arrays.fill(this.rows, this.defaultRow);
        this.makeRow = makeRow;
    }

    public void clear() {
        Arrays.fill(this.rows, this.defaultRow);
    }

    @Nullable
    public T get(int codePoint) {
        int i = codePoint >> 8;
        int j = codePoint & 0xFF;
        return this.rows[i][j];
    }

    @Nullable
    public T put(int codePoint, T glyph) {
        int i = codePoint >> 8;
        int j = codePoint & 0xFF;
        T[] objects = this.rows[i];
        if (objects == this.defaultRow) {
            objects = this.makeRow.apply(256);
            this.rows[i] = objects;
            objects[j] = glyph;
            return null;
        }
        T object = objects[j];
        objects[j] = glyph;
        return object;
    }

    public T computeIfAbsent(int codePoint, IntFunction<T> ifAbsent) {
        int i = codePoint >> 8;
        T[] objects = this.rows[i];
        int j = codePoint & 0xFF;
        T object = objects[j];
        if (object != null) {
            return object;
        }
        if (objects == this.defaultRow) {
            objects = this.makeRow.apply(256);
            this.rows[i] = objects;
        }
        T object2 = ifAbsent.apply(codePoint);
        objects[j] = object2;
        return object2;
    }

    @Nullable
    public T remove(int codePoint) {
        int i = codePoint >> 8;
        int j = codePoint & 0xFF;
        T[] objects = this.rows[i];
        if (objects == this.defaultRow) {
            return null;
        }
        T object = objects[j];
        objects[j] = null;
        return object;
    }

    public void forEachGlyph(GlyphConsumer<T> glyphConsumer) {
        for (int i = 0; i < this.rows.length; ++i) {
            T[] objects = this.rows[i];
            if (objects == this.defaultRow) continue;
            for (int j = 0; j < objects.length; ++j) {
                T object = objects[j];
                if (object == null) continue;
                int k = i << 8 | j;
                glyphConsumer.accept(k, object);
            }
        }
    }

    public IntSet getProvidedGlyphs() {
        IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
        this.forEachGlyph((codePoint, glyph) -> intOpenHashSet.add(codePoint));
        return intOpenHashSet;
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface GlyphConsumer<T> {
        public void accept(int var1, T var2);
    }
}

