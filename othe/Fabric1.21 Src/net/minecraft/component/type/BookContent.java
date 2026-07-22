/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import java.util.List;
import net.minecraft.text.RawFilteredPair;

public interface BookContent<T, C> {
    public List<RawFilteredPair<T>> pages();

    public C withPages(List<RawFilteredPair<T>> var1);
}

