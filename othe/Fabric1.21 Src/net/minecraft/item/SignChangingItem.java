/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public interface SignChangingItem {
    public boolean useOnSign(World var1, SignBlockEntity var2, boolean var3, PlayerEntity var4);

    default public boolean canUseOnSignText(SignText signText, PlayerEntity player) {
        return signText.hasText(player);
    }
}

