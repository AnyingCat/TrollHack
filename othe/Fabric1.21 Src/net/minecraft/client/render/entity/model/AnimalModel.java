/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity.model;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public abstract class AnimalModel<E extends Entity>
extends EntityModel<E> {
    private final boolean headScaled;
    private final float childHeadYOffset;
    private final float childHeadZOffset;
    private final float invertedChildHeadScale;
    private final float invertedChildBodyScale;
    private final float childBodyYOffset;

    protected AnimalModel(boolean headScaled, float childHeadYOffset, float childHeadZOffset) {
        this(headScaled, childHeadYOffset, childHeadZOffset, 2.0f, 2.0f, 24.0f);
    }

    protected AnimalModel(boolean headScaled, float childHeadYOffset, float childHeadZOffset, float invertedChildHeadScale, float invertedChildBodyScale, float childBodyYOffset) {
        this(RenderLayer::getEntityCutoutNoCull, headScaled, childHeadYOffset, childHeadZOffset, invertedChildHeadScale, invertedChildBodyScale, childBodyYOffset);
    }

    protected AnimalModel(Function<Identifier, RenderLayer> renderLayerFactory, boolean headScaled, float childHeadYOffset, float childHeadZOffset, float invertedChildHeadScale, float invertedChildBodyScale, float childBodyYOffset) {
        super(renderLayerFactory);
        this.headScaled = headScaled;
        this.childHeadYOffset = childHeadYOffset;
        this.childHeadZOffset = childHeadZOffset;
        this.invertedChildHeadScale = invertedChildHeadScale;
        this.invertedChildBodyScale = invertedChildBodyScale;
        this.childBodyYOffset = childBodyYOffset;
    }

    protected AnimalModel() {
        this(false, 5.0f, 2.0f);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        if (this.child) {
            float f;
            matrices.push();
            if (this.headScaled) {
                f = 1.5f / this.invertedChildHeadScale;
                matrices.scale(f, f, f);
            }
            matrices.translate(0.0f, this.childHeadYOffset / 16.0f, this.childHeadZOffset / 16.0f);
            this.getHeadParts().forEach(modelPart -> modelPart.render(matrices, vertices, light, overlay, color));
            matrices.pop();
            matrices.push();
            f = 1.0f / this.invertedChildBodyScale;
            matrices.scale(f, f, f);
            matrices.translate(0.0f, this.childBodyYOffset / 16.0f, 0.0f);
            this.getBodyParts().forEach(modelPart -> modelPart.render(matrices, vertices, light, overlay, color));
            matrices.pop();
        } else {
            this.getHeadParts().forEach(modelPart -> modelPart.render(matrices, vertices, light, overlay, color));
            this.getBodyParts().forEach(modelPart -> modelPart.render(matrices, vertices, light, overlay, color));
        }
    }

    protected abstract Iterable<ModelPart> getHeadParts();

    protected abstract Iterable<ModelPart> getBodyParts();
}

