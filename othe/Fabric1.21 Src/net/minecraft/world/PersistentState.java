/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.RegistryWrapper;
import org.slf4j.Logger;

public abstract class PersistentState {
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean dirty;

    public abstract NbtCompound writeNbt(NbtCompound var1, RegistryWrapper.WrapperLookup var2);

    public void markDirty() {
        this.setDirty(true);
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void save(File file, RegistryWrapper.WrapperLookup registryLookup) {
        if (!this.isDirty()) {
            return;
        }
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.put("data", this.writeNbt(new NbtCompound(), registryLookup));
        NbtHelper.putDataVersion(nbtCompound);
        try {
            NbtIo.writeCompressed(nbtCompound, file.toPath());
        } catch (IOException iOException) {
            LOGGER.error("Could not save data {}", (Object)this, (Object)iOException);
        }
        this.setDirty(false);
    }

    public record Type<T extends PersistentState>(Supplier<T> constructor, BiFunction<NbtCompound, RegistryWrapper.WrapperLookup, T> deserializer, DataFixTypes type) {
    }
}

