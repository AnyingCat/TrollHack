/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.FixedBufferInputStream;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PersistentStateManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, PersistentState> loadedStates = Maps.newHashMap();
    private final DataFixer dataFixer;
    private final RegistryWrapper.WrapperLookup registryLookup;
    private final File directory;

    public PersistentStateManager(File directory, DataFixer dataFixer, RegistryWrapper.WrapperLookup registryLookup) {
        this.dataFixer = dataFixer;
        this.directory = directory;
        this.registryLookup = registryLookup;
    }

    private File getFile(String id) {
        return new File(this.directory, id + ".dat");
    }

    public <T extends PersistentState> T getOrCreate(PersistentState.Type<T> type, String id) {
        T persistentState = this.get(type, id);
        if (persistentState != null) {
            return persistentState;
        }
        PersistentState persistentState2 = (PersistentState)type.constructor().get();
        this.set(id, persistentState2);
        return (T)persistentState2;
    }

    @Nullable
    public <T extends PersistentState> T get(PersistentState.Type<T> type, String id) {
        PersistentState persistentState = this.loadedStates.get(id);
        if (persistentState == null && !this.loadedStates.containsKey(id)) {
            persistentState = this.readFromFile(type.deserializer(), type.type(), id);
            this.loadedStates.put(id, persistentState);
        }
        return (T)persistentState;
    }

    @Nullable
    private <T extends PersistentState> T readFromFile(BiFunction<NbtCompound, RegistryWrapper.WrapperLookup, T> readFunction, DataFixTypes dataFixTypes, String id) {
        try {
            File file = this.getFile(id);
            if (file.exists()) {
                NbtCompound nbtCompound = this.readNbt(id, dataFixTypes, SharedConstants.getGameVersion().getSaveVersion().getId());
                return (T)((PersistentState)readFunction.apply(nbtCompound.getCompound("data"), this.registryLookup));
            }
        } catch (Exception exception) {
            LOGGER.error("Error loading saved data: {}", (Object)id, (Object)exception);
        }
        return null;
    }

    public void set(String id, PersistentState state) {
        this.loadedStates.put(id, state);
    }

    public NbtCompound readNbt(String id, DataFixTypes dataFixTypes, int currentSaveVersion) throws IOException {
        File file = this.getFile(id);
        try (FileInputStream inputStream = new FileInputStream(file);){
            NbtCompound nbtCompound;
            try (PushbackInputStream pushbackInputStream = new PushbackInputStream(new FixedBufferInputStream(inputStream), 2);){
                NbtCompound nbtCompound2;
                if (this.isCompressed(pushbackInputStream)) {
                    nbtCompound2 = NbtIo.readCompressed(pushbackInputStream, NbtSizeTracker.ofUnlimitedBytes());
                } else {
                    try (DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);){
                        nbtCompound2 = NbtIo.readCompound(dataInputStream);
                    }
                }
                int i = NbtHelper.getDataVersion(nbtCompound2, 1343);
                nbtCompound = dataFixTypes.update(this.dataFixer, nbtCompound2, i, currentSaveVersion);
            }
            return nbtCompound;
        }
    }

    private boolean isCompressed(PushbackInputStream stream) throws IOException {
        int j;
        byte[] bs = new byte[2];
        boolean bl = false;
        int i = stream.read(bs, 0, 2);
        if (i == 2 && (j = (bs[1] & 0xFF) << 8 | bs[0] & 0xFF) == 35615) {
            bl = true;
        }
        if (i != 0) {
            stream.unread(bs, 0, i);
        }
        return bl;
    }

    public void save() {
        this.loadedStates.forEach((id, state) -> {
            if (state != null) {
                state.save(this.getFile((String)id), this.registryLookup);
            }
        });
    }
}

