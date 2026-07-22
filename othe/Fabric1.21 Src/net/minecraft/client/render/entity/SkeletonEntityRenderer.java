/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class SkeletonEntityRenderer<T extends AbstractSkeletonEntity>
extends BipedEntityRenderer<T, SkeletonEntityModel<T>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/skeleton/skeleton.png");

    public SkeletonEntityRenderer(EntityRendererFactory.Context context) {
        this(context, EntityModelLayers.SKELETON, EntityModelLayers.SKELETON_INNER_ARMOR, EntityModelLayers.SKELETON_OUTER_ARMOR);
    }

    public SkeletonEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, EntityModelLayer legArmorLayer, EntityModelLayer bodyArmorLayer) {
        this(ctx, legArmorLayer, bodyArmorLayer, new SkeletonEntityModel(ctx.getPart(layer)));
    }

    public SkeletonEntityRenderer(EntityRendererFactory.Context context, EntityModelLayer entityModelLayer, EntityModelLayer entityModelLayer2, SkeletonEntityModel<T> skeletonEntityModel) {
        super(context, skeletonEntityModel, 0.5f);
        this.addFeature(new ArmorFeatureRenderer(this, new SkeletonEntityModel(context.getPart(entityModelLayer)), new SkeletonEntityModel(context.getPart(entityModelLayer2)), context.getModelManager()));
    }

    @Override
    public Identifier getTexture(T abstractSkeletonEntity) {
        return TEXTURE;
    }

    @Override
    protected boolean isShaking(T abstractSkeletonEntity) {
        return ((AbstractSkeletonEntity)abstractSkeletonEntity).isShaking();
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntity entity) {
        return this.isShaking((T)((AbstractSkeletonEntity)entity));
    }
}

