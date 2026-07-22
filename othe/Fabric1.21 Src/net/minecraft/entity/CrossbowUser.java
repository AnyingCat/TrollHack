/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public interface CrossbowUser
extends RangedAttackMob {
    public void setCharging(boolean var1);

    @Nullable
    public LivingEntity getTarget();

    public void postShoot();

    default public void shoot(LivingEntity entity, float speed) {
        Hand hand = ProjectileUtil.getHandPossiblyHolding(entity, Items.CROSSBOW);
        ItemStack itemStack = entity.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (item instanceof CrossbowItem) {
            CrossbowItem crossbowItem = (CrossbowItem)item;
            crossbowItem.shootAll(entity.getWorld(), entity, hand, itemStack, speed, 14 - entity.getWorld().getDifficulty().getId() * 4, this.getTarget());
        }
        this.postShoot();
    }
}

