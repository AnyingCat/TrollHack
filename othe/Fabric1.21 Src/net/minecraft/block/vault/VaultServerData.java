/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.vault;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.MathHelper;

public class VaultServerData {
    static final String SERVER_DATA_KEY = "server_data";
    static Codec<VaultServerData> codec = RecordCodecBuilder.create(instance -> instance.group(Uuids.LINKED_SET_CODEC.lenientOptionalFieldOf("rewarded_players", Set.of()).forGetter(data -> data.rewardedPlayers), Codec.LONG.lenientOptionalFieldOf("state_updating_resumes_at", 0L).forGetter(data -> data.stateUpdatingResumesAt), ItemStack.CODEC.listOf().lenientOptionalFieldOf("items_to_eject", List.of()).forGetter(data -> data.itemsToEject), Codec.INT.lenientOptionalFieldOf("total_ejections_needed", 0).forGetter(data -> data.totalEjectionsNeeded)).apply((Applicative<VaultServerData, ?>)instance, VaultServerData::new));
    private static final int MAX_STORED_REWARDED_PLAYERS = 128;
    private final Set<UUID> rewardedPlayers = new ObjectLinkedOpenHashSet<UUID>();
    private long stateUpdatingResumesAt;
    private final List<ItemStack> itemsToEject = new ObjectArrayList<ItemStack>();
    private long lastFailedUnlockTime;
    private int totalEjectionsNeeded;
    boolean dirty;

    VaultServerData(Set<UUID> rewardedPlayers, long stateUpdatingResumesAt, List<ItemStack> itemsToEject, int totalEjectionsNeeded) {
        this.rewardedPlayers.addAll(rewardedPlayers);
        this.stateUpdatingResumesAt = stateUpdatingResumesAt;
        this.itemsToEject.addAll(itemsToEject);
        this.totalEjectionsNeeded = totalEjectionsNeeded;
    }

    VaultServerData() {
    }

    void setLastFailedUnlockTime(long lastFailedUnlockTime) {
        this.lastFailedUnlockTime = lastFailedUnlockTime;
    }

    long getLastFailedUnlockTime() {
        return this.lastFailedUnlockTime;
    }

    Set<UUID> getRewardedPlayers() {
        return this.rewardedPlayers;
    }

    boolean hasRewardedPlayer(PlayerEntity player) {
        return this.rewardedPlayers.contains(player.getUuid());
    }

    @VisibleForTesting
    public void markPlayerAsRewarded(PlayerEntity player) {
        Iterator<UUID> iterator;
        this.rewardedPlayers.add(player.getUuid());
        if (this.rewardedPlayers.size() > 128 && (iterator = this.rewardedPlayers.iterator()).hasNext()) {
            iterator.next();
            iterator.remove();
        }
        this.markDirty();
    }

    long getStateUpdatingResumeTime() {
        return this.stateUpdatingResumesAt;
    }

    void setStateUpdatingResumeTime(long stateUpdatingResumesAt) {
        this.stateUpdatingResumesAt = stateUpdatingResumesAt;
        this.markDirty();
    }

    List<ItemStack> getItemsToEject() {
        return this.itemsToEject;
    }

    void finishEjecting() {
        this.totalEjectionsNeeded = 0;
        this.markDirty();
    }

    void setItemsToEject(List<ItemStack> itemsToEject) {
        this.itemsToEject.clear();
        this.itemsToEject.addAll(itemsToEject);
        this.totalEjectionsNeeded = this.itemsToEject.size();
        this.markDirty();
    }

    ItemStack getItemToDisplay() {
        if (this.itemsToEject.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return Objects.requireNonNullElse(this.itemsToEject.get(this.itemsToEject.size() - 1), ItemStack.EMPTY);
    }

    ItemStack getItemToEject() {
        if (this.itemsToEject.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.markDirty();
        return Objects.requireNonNullElse(this.itemsToEject.remove(this.itemsToEject.size() - 1), ItemStack.EMPTY);
    }

    void copyFrom(VaultServerData data) {
        this.stateUpdatingResumesAt = data.getStateUpdatingResumeTime();
        this.itemsToEject.clear();
        this.itemsToEject.addAll(data.itemsToEject);
        this.rewardedPlayers.clear();
        this.rewardedPlayers.addAll(data.rewardedPlayers);
    }

    private void markDirty() {
        this.dirty = true;
    }

    public float getEjectSoundPitchModifier() {
        if (this.totalEjectionsNeeded == 1) {
            return 1.0f;
        }
        return 1.0f - MathHelper.getLerpProgress(this.getItemsToEject().size(), 1.0f, this.totalEjectionsNeeded);
    }
}

