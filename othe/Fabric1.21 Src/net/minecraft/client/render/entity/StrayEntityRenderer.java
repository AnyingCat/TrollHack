/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.feature.SkeletonOverlayFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class StrayEntityRenderer
extends SkeletonEntityRenderer<StrayEntity> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/skeleton/stray.png");
    private static final Identifier field_49165 = Identifier.ofVanilla("textures/entity/skeleton/stray_overlay.png");

    public StrayEntityRenderer(EntityRendererFactory.Context context) {
        super(context, EntityModelLayers.STRAY, EntityModelLayers.STRAY_INNER_ARMOR, EntityModelLayers.STRAY_OUTER_ARMOR);
        this.addFeature(new SkeletonOverlayFeatureRenderer<StrayEntity, SkeletonEntityModel<StrayEntity>>(this, context.getModelLoader(), EntityModelLayers.STRAY_OUTER, field_49165));
    }

    @Override
    public Identifier getTexture(StrayEntity strayEntity) {
        return TEXTURE;
    }
}

