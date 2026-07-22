/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

@Environment(value=EnvType.CLIENT)
public abstract class MobEntityRenderer<T extends MobEntity, M extends EntityModel<T>>
extends LivingEntityRenderer<T, M> {
    public MobEntityRenderer(EntityRendererFactory.Context context, M entityModel, float f) {
        super(context, entityModel, f);
    }

    @Override
    protected boolean hasLabel(T mobEntity) {
        return super.hasLabel(mobEntity) && (((LivingEntity)mobEntity).shouldRenderName() || ((Entity)mobEntity).hasCustomName() && mobEntity == this.dispatcher.targetedEntity);
    }

    @Override
    protected float getShadowRadius(T mobEntity) {
        return super.getShadowRadius(mobEntity) * ((LivingEntity)mobEntity).getScaleFactor();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntity livingEntity) {
        return this.getShadowRadius((T)((MobEntity)livingEntity));
    }

    @Override
    protected /* synthetic */ boolean hasLabel(LivingEntity livingEntity) {
        return this.hasLabel((T)((MobEntity)livingEntity));
    }

    @Override
    protected /* synthetic */ float getShadowRadius(Entity entity) {
        return this.getShadowRadius((T)((MobEntity)entity));
    }

    @Override
    protected /* synthetic */ boolean hasLabel(Entity entity) {
        return this.hasLabel((T)((MobEntity)entity));
    }
}

