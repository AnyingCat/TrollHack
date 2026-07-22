/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Deprecated
@Environment(value=EnvType.CLIENT)
public interface ModelPredicateProvider {
    public float call(ItemStack var1, @Nullable ClientWorld var2, @Nullable LivingEntity var3, int var4);
}

