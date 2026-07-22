/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageKey;

public interface ChunkErrorHandler {
    public void onChunkLoadFailure(Throwable var1, StorageKey var2, ChunkPos var3);

    public void onChunkSaveFailure(Throwable var1, StorageKey var2, ChunkPos var3);

    public static CrashException createMisplacementException(ChunkPos actualPos, ChunkPos expectedPos) {
        CrashReport crashReport = CrashReport.create(new IllegalStateException("Retrieved chunk position " + String.valueOf(actualPos) + " does not match requested " + String.valueOf(expectedPos)), "Chunk found in invalid location");
        CrashReportSection crashReportSection = crashReport.addElement("Misplaced Chunk");
        crashReportSection.add("Stored Position", actualPos::toString);
        return new CrashException(crashReport);
    }

    default public void onChunkMisplacement(ChunkPos actualPos, ChunkPos expectedPos, StorageKey key) {
        this.onChunkLoadFailure(ChunkErrorHandler.createMisplacementException(actualPos, expectedPos), key, expectedPos);
    }
}

