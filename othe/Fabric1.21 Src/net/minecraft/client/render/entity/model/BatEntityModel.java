/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.animation.BatAnimations;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.passive.BatEntity;

/**
 * Represents the model of a {@linkplain BatEntity}.
 * 
 * <div class="fabric">
 * <table border=1>
 * <caption>Model parts of this model</caption>
 * <tr>
 *   <th>Part Name</th><th>Parent</th><th>Corresponding Field</th>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#HEAD}</td><td>{@linkplain #root Root part}</td><td>{@link #head}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#RIGHT_EAR}</td><td>{@value EntityModelPartNames#HEAD}</td><td></td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#LEFT_EAR}</td><td>{@value EntityModelPartNames#HEAD}</td><td></td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#BODY}</td><td>{@linkplain #root Root part}</td><td>{@link #body}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#RIGHT_WING}</td><td>{@value EntityModelPartNames#BODY}</td><td>{@link #rightWing}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#RIGHT_WING_TIP}</td><td>{@value EntityModelPartNames#RIGHT_WING}</td><td>{@link #rightWingTip}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#LEFT_WING}</td><td>{@value EntityModelPartNames#BODY}</td><td>{@link #leftWing}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#LEFT_WING_TIP}</td><td>{@value EntityModelPartNames#LEFT_WING}</td><td>{@link #leftWingTip}</td>
 * </tr>
 * </table>
 * </div>
 */
@Environment(value=EnvType.CLIENT)
public class BatEntityModel
extends SinglePartEntityModel<BatEntity> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart rightWingTip;
    private final ModelPart leftWingTip;
    private final ModelPart feet;

    public BatEntityModel(ModelPart root) {
        super(RenderLayer::getEntityCutout);
        this.root = root;
        this.body = root.getChild(EntityModelPartNames.BODY);
        this.head = root.getChild(EntityModelPartNames.HEAD);
        this.rightWing = this.body.getChild(EntityModelPartNames.RIGHT_WING);
        this.rightWingTip = this.rightWing.getChild(EntityModelPartNames.RIGHT_WING_TIP);
        this.leftWing = this.body.getChild(EntityModelPartNames.LEFT_WING);
        this.leftWingTip = this.leftWing.getChild(EntityModelPartNames.LEFT_WING_TIP);
        this.feet = this.body.getChild(EntityModelPartNames.FEET);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData modelPartData2 = modelPartData.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-1.5f, 0.0f, -1.0f, 3.0f, 5.0f, 2.0f), ModelTransform.pivot(0.0f, 17.0f, 0.0f));
        ModelPartData modelPartData3 = modelPartData.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 7).cuboid(-2.0f, -3.0f, -1.0f, 4.0f, 3.0f, 2.0f), ModelTransform.pivot(0.0f, 17.0f, 0.0f));
        modelPartData3.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create().uv(1, 15).cuboid(-2.5f, -4.0f, 0.0f, 3.0f, 5.0f, 0.0f), ModelTransform.pivot(-1.5f, -2.0f, 0.0f));
        modelPartData3.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create().uv(8, 15).cuboid(-0.1f, -3.0f, 0.0f, 3.0f, 5.0f, 0.0f), ModelTransform.pivot(1.1f, -3.0f, 0.0f));
        ModelPartData modelPartData4 = modelPartData2.addChild(EntityModelPartNames.RIGHT_WING, ModelPartBuilder.create().uv(12, 0).cuboid(-2.0f, -2.0f, 0.0f, 2.0f, 7.0f, 0.0f), ModelTransform.pivot(-1.5f, 0.0f, 0.0f));
        modelPartData4.addChild(EntityModelPartNames.RIGHT_WING_TIP, ModelPartBuilder.create().uv(16, 0).cuboid(-6.0f, -2.0f, 0.0f, 6.0f, 8.0f, 0.0f), ModelTransform.pivot(-2.0f, 0.0f, 0.0f));
        ModelPartData modelPartData5 = modelPartData2.addChild(EntityModelPartNames.LEFT_WING, ModelPartBuilder.create().uv(12, 7).cuboid(0.0f, -2.0f, 0.0f, 2.0f, 7.0f, 0.0f), ModelTransform.pivot(1.5f, 0.0f, 0.0f));
        modelPartData5.addChild(EntityModelPartNames.LEFT_WING_TIP, ModelPartBuilder.create().uv(16, 8).cuboid(0.0f, -2.0f, 0.0f, 6.0f, 8.0f, 0.0f), ModelTransform.pivot(2.0f, 0.0f, 0.0f));
        modelPartData2.addChild(EntityModelPartNames.FEET, ModelPartBuilder.create().uv(16, 16).cuboid(-1.5f, 0.0f, 0.0f, 3.0f, 2.0f, 0.0f), ModelTransform.pivot(0.0f, 5.0f, 0.0f));
        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(BatEntity batEntity, float f, float g, float h, float i, float j) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        if (batEntity.isRoosting()) {
            this.setRoostingHeadAngles(i);
        }
        this.updateAnimation(batEntity.flyingAnimationState, BatAnimations.FLYING, h, 1.0f);
        this.updateAnimation(batEntity.roostingAnimationState, BatAnimations.ROOSTING, h, 1.0f);
    }

    private void setRoostingHeadAngles(float yaw) {
        this.head.yaw = yaw * ((float)Math.PI / 180);
    }
}

