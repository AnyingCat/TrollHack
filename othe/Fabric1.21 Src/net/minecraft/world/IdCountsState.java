/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

public class IdCountsState
extends PersistentState {
    public static final String IDCOUNTS_KEY = "idcounts";
    private final Object2IntMap<String> idCounts = new Object2IntOpenHashMap<String>();

    public static PersistentState.Type<IdCountsState> getPersistentStateType() {
        return new PersistentState.Type<IdCountsState>(IdCountsState::new, IdCountsState::fromNbt, DataFixTypes.SAVED_DATA_MAP_INDEX);
    }

    public IdCountsState() {
        this.idCounts.defaultReturnValue(-1);
    }

    public static IdCountsState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        IdCountsState idCountsState = new IdCountsState();
        for (String string : nbt.getKeys()) {
            if (!nbt.contains(string, NbtElement.NUMBER_TYPE)) continue;
            idCountsState.idCounts.put(string, nbt.getInt(string));
        }
        return idCountsState;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        for (Object2IntMap.Entry entry : this.idCounts.object2IntEntrySet()) {
            nbt.putInt((String)entry.getKey(), entry.getIntValue());
        }
        return nbt;
    }

    public MapIdComponent increaseAndGetMapId() {
        int i = this.idCounts.getInt("map") + 1;
        this.idCounts.put("map", i);
        this.markDirty();
        return new MapIdComponent(i);
    }
}

