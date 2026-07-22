/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.data;

import java.util.List;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;

public interface DataTracked {
    /**
     * Called on the client when the tracked data is set.
     * 
     * <p>This can be overridden to refresh other fields when the tracked data
     * is set or changed.
     */
    public void onTrackedDataSet(TrackedData<?> var1);

    public void onDataTrackerUpdate(List<DataTracker.SerializedEntry<?>> var1);
}

