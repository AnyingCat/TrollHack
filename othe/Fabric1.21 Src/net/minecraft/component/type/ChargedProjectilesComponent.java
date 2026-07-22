/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public final class ChargedProjectilesComponent {
    public static final ChargedProjectilesComponent DEFAULT = new ChargedProjectilesComponent(List.of());
    public static final Codec<ChargedProjectilesComponent> CODEC = ItemStack.CODEC.listOf().xmap(ChargedProjectilesComponent::new, chargedProjectilesComponent -> chargedProjectilesComponent.projectiles);
    public static final PacketCodec<RegistryByteBuf, ChargedProjectilesComponent> PACKET_CODEC = ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(ChargedProjectilesComponent::new, chargedProjectilesComponent -> chargedProjectilesComponent.projectiles);
    private final List<ItemStack> projectiles;

    private ChargedProjectilesComponent(List<ItemStack> projectiles) {
        this.projectiles = projectiles;
    }

    public static ChargedProjectilesComponent of(ItemStack projectile) {
        return new ChargedProjectilesComponent(List.of(projectile.copy()));
    }

    public static ChargedProjectilesComponent of(List<ItemStack> projectiles) {
        return new ChargedProjectilesComponent(List.copyOf(Lists.transform(projectiles, ItemStack::copy)));
    }

    public boolean contains(Item item) {
        for (ItemStack itemStack : this.projectiles) {
            if (!itemStack.isOf(item)) continue;
            return true;
        }
        return false;
    }

    public List<ItemStack> getProjectiles() {
        return Lists.transform(this.projectiles, ItemStack::copy);
    }

    public boolean isEmpty() {
        return this.projectiles.isEmpty();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChargedProjectilesComponent)) return false;
        ChargedProjectilesComponent chargedProjectilesComponent = (ChargedProjectilesComponent)o;
        if (!ItemStack.stacksEqual(this.projectiles, chargedProjectilesComponent.projectiles)) return false;
        return true;
    }

    public int hashCode() {
        return ItemStack.listHashCode(this.projectiles);
    }

    public String toString() {
        return "ChargedProjectiles[items=" + String.valueOf(this.projectiles) + "]";
    }
}

