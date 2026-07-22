/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class DataCommandStorage {
    private static final String COMMAND_STORAGE_PREFIX = "command_storage_";
    private final Map<String, PersistentState> storages = Maps.newHashMap();
    private final PersistentStateManager stateManager;

    public DataCommandStorage(PersistentStateManager stateManager) {
        this.stateManager = stateManager;
    }

    private PersistentState createStorage(String namespace) {
        PersistentState persistentState = new PersistentState();
        this.storages.put(namespace, persistentState);
        return persistentState;
    }

    private PersistentState.Type<PersistentState> getPersistentStateType(String namespace) {
        return new PersistentState.Type<PersistentState>(() -> this.createStorage(namespace), (nbt, registryLookup) -> this.createStorage(namespace).readNbt((NbtCompound)nbt), DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public NbtCompound get(Identifier id) {
        String string = id.getNamespace();
        PersistentState persistentState = this.stateManager.get(this.getPersistentStateType(string), DataCommandStorage.getSaveKey(string));
        return persistentState != null ? persistentState.get(id.getPath()) : new NbtCompound();
    }

    public void set(Identifier id, NbtCompound nbt) {
        String string = id.getNamespace();
        this.stateManager.getOrCreate(this.getPersistentStateType(string), DataCommandStorage.getSaveKey(string)).set(id.getPath(), nbt);
    }

    public Stream<Identifier> getIds() {
        return this.storages.entrySet().stream().flatMap(entry -> ((PersistentState)entry.getValue()).getIds((String)entry.getKey()));
    }

    private static String getSaveKey(String namespace) {
        return COMMAND_STORAGE_PREFIX + namespace;
    }

    static class PersistentState
    extends net.minecraft.world.PersistentState {
        private static final String CONTENTS_KEY = "contents";
        private final Map<String, NbtCompound> map = Maps.newHashMap();

        PersistentState() {
        }

        PersistentState readNbt(NbtCompound nbt) {
            NbtCompound nbtCompound = nbt.getCompound(CONTENTS_KEY);
            for (String string : nbtCompound.getKeys()) {
                this.map.put(string, nbtCompound.getCompound(string));
            }
            return this;
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
            NbtCompound nbtCompound = new NbtCompound();
            this.map.forEach((key, value) -> nbtCompound.put((String)key, value.copy()));
            nbt.put(CONTENTS_KEY, nbtCompound);
            return nbt;
        }

        public NbtCompound get(String name) {
            NbtCompound nbtCompound = this.map.get(name);
            return nbtCompound != null ? nbtCompound : new NbtCompound();
        }

        public void set(String name, NbtCompound nbt) {
            if (nbt.isEmpty()) {
                this.map.remove(name);
            } else {
                this.map.put(name, nbt);
            }
            this.markDirty();
        }

        public Stream<Identifier> getIds(String namespace) {
            return this.map.keySet().stream().map(key -> Identifier.of(namespace, key));
        }
    }
}

