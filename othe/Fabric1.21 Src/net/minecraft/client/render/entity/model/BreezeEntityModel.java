/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.animation.BreezeAnimations;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class BreezeEntityModel<T extends BreezeEntity>
extends SinglePartEntityModel<T> {
    private static final float field_47431 = 0.6f;
    private static final float field_47432 = 0.8f;
    private static final float field_47433 = 1.0f;
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart eyes;
    private final ModelPart windBody;
    private final ModelPart windTop;
    private final ModelPart windMid;
    private final ModelPart windBottom;
    private final ModelPart rods;

    public BreezeEntityModel(ModelPart root) {
        super(RenderLayer::getEntityTranslucent);
        this.root = root;
        this.windBody = root.getChild(EntityModelPartNames.WIND_BODY);
        this.windBottom = this.windBody.getChild(EntityModelPartNames.WIND_BOTTOM);
        this.windMid = this.windBottom.getChild(EntityModelPartNames.WIND_MID);
        this.windTop = this.windMid.getChild(EntityModelPartNames.WIND_TOP);
        this.head = root.getChild(EntityModelPartNames.BODY).getChild(EntityModelPartNames.HEAD);
        this.eyes = this.head.getChild(EntityModelPartNames.EYES);
        this.rods = root.getChild(EntityModelPartNames.BODY).getChild(EntityModelPartNames.RODS);
    }

    public static TexturedModelData getTexturedModelData(int textureWidth, int textureHeight) {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData modelPartData2 = modelPartData.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData modelPartData3 = modelPartData2.addChild(EntityModelPartNames.RODS, ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 8.0f, 0.0f));
        modelPartData3.addChild("rod_1", ModelPartBuilder.create().uv(0, 17).cuboid(-1.0f, 0.0f, -3.0f, 2.0f, 8.0f, 2.0f, new Dilation(0.0f)), ModelTransform.of(2.5981f, -3.0f, 1.5f, -2.7489f, -1.0472f, 3.1416f));
        modelPartData3.addChild("rod_2", ModelPartBuilder.create().uv(0, 17).cuboid(-1.0f, 0.0f, -3.0f, 2.0f, 8.0f, 2.0f, new Dilation(0.0f)), ModelTransform.of(-2.5981f, -3.0f, 1.5f, -2.7489f, 1.0472f, 3.1416f));
        modelPartData3.addChild("rod_3", ModelPartBuilder.create().uv(0, 17).cuboid(-1.0f, 0.0f, -3.0f, 2.0f, 8.0f, 2.0f, new Dilation(0.0f)), ModelTransform.of(0.0f, -3.0f, -3.0f, 0.3927f, 0.0f, 0.0f));
        ModelPartData modelPartData4 = modelPartData2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(4, 24).cuboid(-5.0f, -5.0f, -4.2f, 10.0f, 3.0f, 4.0f, new Dilation(0.0f)).uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, 4.0f, 0.0f));
        modelPartData4.addChild(EntityModelPartNames.EYES, ModelPartBuilder.create().uv(4, 24).cuboid(-5.0f, -5.0f, -4.2f, 10.0f, 3.0f, 4.0f, new Dilation(0.0f)).uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData modelPartData5 = modelPartData.addChild(EntityModelPartNames.WIND_BODY, ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData modelPartData6 = modelPartData5.addChild(EntityModelPartNames.WIND_BOTTOM, ModelPartBuilder.create().uv(1, 83).cuboid(-2.5f, -7.0f, -2.5f, 5.0f, 7.0f, 5.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, 24.0f, 0.0f));
        ModelPartData modelPartData7 = modelPartData6.addChild(EntityModelPartNames.WIND_MID, ModelPartBuilder.create().uv(74, 28).cuboid(-6.0f, -6.0f, -6.0f, 12.0f, 6.0f, 12.0f, new Dilation(0.0f)).uv(78, 32).cuboid(-4.0f, -6.0f, -4.0f, 8.0f, 6.0f, 8.0f, new Dilation(0.0f)).uv(49, 71).cuboid(-2.5f, -6.0f, -2.5f, 5.0f, 6.0f, 5.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, -7.0f, 0.0f));
        modelPartData7.addChild(EntityModelPartNames.WIND_TOP, ModelPartBuilder.create().uv(0, 0).cuboid(-9.0f, -8.0f, -9.0f, 18.0f, 8.0f, 18.0f, new Dilation(0.0f)).uv(6, 6).cuboid(-6.0f, -8.0f, -6.0f, 12.0f, 8.0f, 12.0f, new Dilation(0.0f)).uv(105, 57).cuboid(-2.5f, -8.0f, -2.5f, 5.0f, 8.0f, 5.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, -6.0f, 0.0f));
        return TexturedModelData.of(modelData, textureWidth, textureHeight);
    }

    @Override
    public void setAngles(T breezeEntity, float f, float g, float h, float i, float j) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        float k = h * (float)Math.PI * -0.1f;
        this.windTop.pivotX = MathHelper.cos(k) * 1.0f * 0.6f;
        this.windTop.pivotZ = MathHelper.sin(k) * 1.0f * 0.6f;
        this.windMid.pivotX = MathHelper.sin(k) * 0.5f * 0.8f;
        this.windMid.pivotZ = MathHelper.cos(k) * 0.8f;
        this.windBottom.pivotX = MathHelper.cos(k) * -0.25f * 1.0f;
        this.windBottom.pivotZ = MathHelper.sin(k) * -0.25f * 1.0f;
        this.head.pivotY = 4.0f + MathHelper.cos(k) / 4.0f;
        this.rods.yaw = h * (float)Math.PI * 0.1f;
        this.updateAnimation(((BreezeEntity)breezeEntity).shootingAnimationState, BreezeAnimations.SHOOTING, h);
        this.updateAnimation(((BreezeEntity)breezeEntity).slidingAnimationState, BreezeAnimations.SLIDING, h);
        this.updateAnimation(((BreezeEntity)breezeEntity).field_47816, BreezeAnimations.field_47846, h);
        this.updateAnimation(((BreezeEntity)breezeEntity).inhalingAnimationState, BreezeAnimations.INHALING, h);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    public ModelPart getHead() {
        return this.head;
    }

    public ModelPart getEyes() {
        return this.eyes;
    }

    public ModelPart getRods() {
        return this.rods;
    }

    public ModelPart getWindBody() {
        return this.windBody;
    }
}

