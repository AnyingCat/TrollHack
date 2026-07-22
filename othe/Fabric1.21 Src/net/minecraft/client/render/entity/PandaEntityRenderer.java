/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.PandaHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PandaEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class PandaEntityRenderer
extends MobEntityRenderer<PandaEntity, PandaEntityModel<PandaEntity>> {
    private static final Map<PandaEntity.Gene, Identifier> TEXTURES = Util.make(Maps.newEnumMap(PandaEntity.Gene.class), map -> {
        map.put(PandaEntity.Gene.NORMAL, Identifier.ofVanilla("textures/entity/panda/panda.png"));
        map.put(PandaEntity.Gene.LAZY, Identifier.ofVanilla("textures/entity/panda/lazy_panda.png"));
        map.put(PandaEntity.Gene.WORRIED, Identifier.ofVanilla("textures/entity/panda/worried_panda.png"));
        map.put(PandaEntity.Gene.PLAYFUL, Identifier.ofVanilla("textures/entity/panda/playful_panda.png"));
        map.put(PandaEntity.Gene.BROWN, Identifier.ofVanilla("textures/entity/panda/brown_panda.png"));
        map.put(PandaEntity.Gene.WEAK, Identifier.ofVanilla("textures/entity/panda/weak_panda.png"));
        map.put(PandaEntity.Gene.AGGRESSIVE, Identifier.ofVanilla("textures/entity/panda/aggressive_panda.png"));
    });

    public PandaEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PandaEntityModel(context.getPart(EntityModelLayers.PANDA)), 0.9f);
        this.addFeature(new PandaHeldItemFeatureRenderer(this, context.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(PandaEntity pandaEntity) {
        return TEXTURES.getOrDefault(pandaEntity.getProductGene(), TEXTURES.get(PandaEntity.Gene.NORMAL));
    }

    @Override
    protected void setupTransforms(PandaEntity pandaEntity, MatrixStack matrixStack, float f, float g, float h, float i) {
        float s;
        float r;
        float l;
        super.setupTransforms(pandaEntity, matrixStack, f, g, h, i);
        if (pandaEntity.playingTicks > 0) {
            float m;
            int j = pandaEntity.playingTicks;
            int k = j + 1;
            l = 7.0f;
            float f2 = m = pandaEntity.isBaby() ? 0.3f : 0.8f;
            if (j < 8) {
                float n = (float)(90 * j) / 7.0f;
                float o = (float)(90 * k) / 7.0f;
                float p = this.getAngle(n, o, k, h, 8.0f);
                matrixStack.translate(0.0f, (m + 0.2f) * (p / 90.0f), 0.0f);
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-p));
            } else if (j < 16) {
                float n = ((float)j - 8.0f) / 7.0f;
                float o = 90.0f + 90.0f * n;
                float q = 90.0f + 90.0f * ((float)k - 8.0f) / 7.0f;
                float p = this.getAngle(o, q, k, h, 16.0f);
                matrixStack.translate(0.0f, m + 0.2f + (m - 0.2f) * (p - 90.0f) / 90.0f, 0.0f);
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-p));
            } else if ((float)j < 24.0f) {
                float n = ((float)j - 16.0f) / 7.0f;
                float o = 180.0f + 90.0f * n;
                float q = 180.0f + 90.0f * ((float)k - 16.0f) / 7.0f;
                float p = this.getAngle(o, q, k, h, 24.0f);
                matrixStack.translate(0.0f, m + m * (270.0f - p) / 90.0f, 0.0f);
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-p));
            } else if (j < 32) {
                float n = ((float)j - 24.0f) / 7.0f;
                float o = 270.0f + 90.0f * n;
                float q = 270.0f + 90.0f * ((float)k - 24.0f) / 7.0f;
                float p = this.getAngle(o, q, k, h, 32.0f);
                matrixStack.translate(0.0f, m * ((360.0f - p) / 90.0f), 0.0f);
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-p));
            }
        }
        if ((r = pandaEntity.getSittingAnimationProgress(h)) > 0.0f) {
            matrixStack.translate(0.0f, 0.8f * r, 0.0f);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.lerp(r, pandaEntity.getPitch(), pandaEntity.getPitch() + 90.0f)));
            matrixStack.translate(0.0f, -1.0f * r, 0.0f);
            if (pandaEntity.isScaredByThunderstorm()) {
                float s2 = (float)(Math.cos((double)pandaEntity.age * 1.25) * Math.PI * (double)0.05f);
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(s2));
                if (pandaEntity.isBaby()) {
                    matrixStack.translate(0.0f, 0.8f, 0.55f);
                }
            }
        }
        if ((s = pandaEntity.getLieOnBackAnimationProgress(h)) > 0.0f) {
            l = pandaEntity.isBaby() ? 0.5f : 1.3f;
            matrixStack.translate(0.0f, l * s, 0.0f);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.lerp(s, pandaEntity.getPitch(), pandaEntity.getPitch() + 180.0f)));
        }
    }

    private float getAngle(float f, float g, int i, float h, float j) {
        if ((float)i < j) {
            return MathHelper.lerp(h, f, g);
        }
        return f;
    }
}

