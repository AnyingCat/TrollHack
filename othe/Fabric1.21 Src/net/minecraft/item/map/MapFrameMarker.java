/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item.map;

import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class MapFrameMarker {
    private final BlockPos pos;
    private final int rotation;
    private final int entityId;

    public MapFrameMarker(BlockPos pos, int rotation, int entityId) {
        this.pos = pos;
        this.rotation = rotation;
        this.entityId = entityId;
    }

    @Nullable
    public static MapFrameMarker fromNbt(NbtCompound nbt) {
        Optional<BlockPos> optional = NbtHelper.toBlockPos(nbt, "pos");
        if (optional.isEmpty()) {
            return null;
        }
        int i = nbt.getInt("rotation");
        int j = nbt.getInt("entity_id");
        return new MapFrameMarker(optional.get(), i, j);
    }

    public NbtCompound toNbt() {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.put("pos", NbtHelper.fromBlockPos(this.pos));
        nbtCompound.putInt("rotation", this.rotation);
        nbtCompound.putInt("entity_id", this.entityId);
        return nbtCompound;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getRotation() {
        return this.rotation;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getKey() {
        return MapFrameMarker.getKey(this.pos);
    }

    public static String getKey(BlockPos pos) {
        return "frame-" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}

