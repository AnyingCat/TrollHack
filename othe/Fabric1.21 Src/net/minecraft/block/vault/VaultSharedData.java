/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.vault;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

public class VaultSharedData {
    static final String SHARED_DATA_KEY = "shared_data";
    static Codec<VaultSharedData> codec = RecordCodecBuilder.create(instance -> instance.group(ItemStack.createOptionalCodec("display_item").forGetter(data -> data.displayItem), Uuids.LINKED_SET_CODEC.lenientOptionalFieldOf("connected_players", Set.of()).forGetter(data -> data.connectedPlayers), Codec.DOUBLE.lenientOptionalFieldOf("connected_particles_range", VaultConfig.DEFAULT.deactivationRange()).forGetter(data -> data.connectedParticlesRange)).apply((Applicative<VaultSharedData, ?>)instance, VaultSharedData::new));
    private ItemStack displayItem = ItemStack.EMPTY;
    private Set<UUID> connectedPlayers = new ObjectLinkedOpenHashSet<UUID>();
    private double connectedParticlesRange = VaultConfig.DEFAULT.deactivationRange();
    boolean dirty;

    VaultSharedData(ItemStack displayItem, Set<UUID> connectedPlayers, double connectedParticlesRange) {
        this.displayItem = displayItem;
        this.connectedPlayers.addAll(connectedPlayers);
        this.connectedParticlesRange = connectedParticlesRange;
    }

    VaultSharedData() {
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public boolean hasDisplayItem() {
        return !this.displayItem.isEmpty();
    }

    public void setDisplayItem(ItemStack stack) {
        if (ItemStack.areEqual(this.displayItem, stack)) {
            return;
        }
        this.displayItem = stack.copy();
        this.markDirty();
    }

    boolean hasConnectedPlayers() {
        return !this.connectedPlayers.isEmpty();
    }

    Set<UUID> getConnectedPlayers() {
        return this.connectedPlayers;
    }

    double getConnectedParticlesRange() {
        return this.connectedParticlesRange;
    }

    void updateConnectedPlayers(ServerWorld world, BlockPos pos, VaultServerData serverData, VaultConfig config, double radius) {
        Set set = config.playerDetector().detect(world, config.entitySelector(), pos, radius, false).stream().filter(uuid -> !serverData.getRewardedPlayers().contains(uuid)).collect(Collectors.toSet());
        if (!this.connectedPlayers.equals(set)) {
            this.connectedPlayers = set;
            this.markDirty();
        }
    }

    private void markDirty() {
        this.dirty = true;
    }

    void copyFrom(VaultSharedData data) {
        this.displayItem = data.displayItem;
        this.connectedPlayers = data.connectedPlayers;
        this.connectedParticlesRange = data.connectedParticlesRange;
    }
}

