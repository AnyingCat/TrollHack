/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import org.jetbrains.annotations.Nullable;

public class PlacedAdvancement {
    private final AdvancementEntry advancementEntry;
    @Nullable
    private final PlacedAdvancement parent;
    private final Set<PlacedAdvancement> children = new ReferenceOpenHashSet<PlacedAdvancement>();

    @VisibleForTesting
    public PlacedAdvancement(AdvancementEntry advancementEntry, @Nullable PlacedAdvancement parent) {
        this.advancementEntry = advancementEntry;
        this.parent = parent;
    }

    public Advancement getAdvancement() {
        return this.advancementEntry.value();
    }

    public AdvancementEntry getAdvancementEntry() {
        return this.advancementEntry;
    }

    @Nullable
    public PlacedAdvancement getParent() {
        return this.parent;
    }

    public PlacedAdvancement getRoot() {
        return PlacedAdvancement.findRoot(this);
    }

    public static PlacedAdvancement findRoot(PlacedAdvancement advancement) {
        PlacedAdvancement placedAdvancement = advancement;
        PlacedAdvancement placedAdvancement2;
        while ((placedAdvancement2 = placedAdvancement.getParent()) != null) {
            placedAdvancement = placedAdvancement2;
        }
        return placedAdvancement;
    }

    public Iterable<PlacedAdvancement> getChildren() {
        return this.children;
    }

    @VisibleForTesting
    public void addChild(PlacedAdvancement advancement) {
        this.children.add(advancement);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlacedAdvancement)) return false;
        PlacedAdvancement placedAdvancement = (PlacedAdvancement)o;
        if (!this.advancementEntry.equals(placedAdvancement.advancementEntry)) return false;
        return true;
    }

    public int hashCode() {
        return this.advancementEntry.hashCode();
    }

    public String toString() {
        return this.advancementEntry.id().toString();
    }
}

