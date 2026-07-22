/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public abstract class DisplayEntityRenderer<T extends DisplayEntity, S>
extends EntityRenderer<T> {
    private final EntityRenderDispatcher renderDispatcher;

    protected DisplayEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.renderDispatcher = context.getRenderDispatcher();
    }

    @Override
    public Identifier getTexture(T displayEntity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public void render(T displayEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        DisplayEntity.RenderState renderState = ((DisplayEntity)displayEntity).getRenderState();
        if (renderState == null) {
            return;
        }
        S object = this.getData(displayEntity);
        if (object == null) {
            return;
        }
        float h = ((DisplayEntity)displayEntity).getLerpProgress(g);
        this.shadowRadius = renderState.shadowRadius().lerp(h);
        this.shadowOpacity = renderState.shadowStrength().lerp(h);
        int j = renderState.brightnessOverride();
        int k = j != -1 ? j : i;
        super.render(displayEntity, f, g, matrixStack, vertexConsumerProvider, k);
        matrixStack.push();
        matrixStack.multiply(this.getBillboardRotation(renderState, displayEntity, g, new Quaternionf()));
        AffineTransformation affineTransformation = renderState.transformation().interpolate(h);
        matrixStack.multiplyPositionMatrix(affineTransformation.getMatrix());
        this.render(displayEntity, object, matrixStack, vertexConsumerProvider, k, h);
        matrixStack.pop();
    }

    private Quaternionf getBillboardRotation(DisplayEntity.RenderState renderState, T entity, float yaw, Quaternionf rotation) {
        Camera camera = this.renderDispatcher.camera;
        return switch (renderState.billboardConstraints()) {
            default -> throw new MatchException(null, null);
            case DisplayEntity.BillboardMode.FIXED -> rotation.rotationYXZ((float)(-Math.PI) / 180 * DisplayEntityRenderer.lerpYaw(entity, yaw), (float)Math.PI / 180 * DisplayEntityRenderer.lerpPitch(entity, yaw), 0.0f);
            case DisplayEntity.BillboardMode.HORIZONTAL -> rotation.rotationYXZ((float)(-Math.PI) / 180 * DisplayEntityRenderer.lerpYaw(entity, yaw), (float)Math.PI / 180 * DisplayEntityRenderer.getNegatedPitch(camera), 0.0f);
            case DisplayEntity.BillboardMode.VERTICAL -> rotation.rotationYXZ((float)(-Math.PI) / 180 * DisplayEntityRenderer.getBackwardsYaw(camera), (float)Math.PI / 180 * DisplayEntityRenderer.lerpPitch(entity, yaw), 0.0f);
            case DisplayEntity.BillboardMode.CENTER -> rotation.rotationYXZ((float)(-Math.PI) / 180 * DisplayEntityRenderer.getBackwardsYaw(camera), (float)Math.PI / 180 * DisplayEntityRenderer.getNegatedPitch(camera), 0.0f);
        };
    }

    private static float getBackwardsYaw(Camera camera) {
        return camera.getYaw() - 180.0f;
    }

    private static float getNegatedPitch(Camera camera) {
        return -camera.getPitch();
    }

    private static <T extends DisplayEntity> float lerpYaw(T entity, float delta) {
        return MathHelper.lerpAngleDegrees(delta, entity.prevYaw, entity.getYaw());
    }

    private static <T extends DisplayEntity> float lerpPitch(T entity, float delta) {
        return MathHelper.lerp(delta, entity.prevPitch, entity.getPitch());
    }

    @Nullable
    protected abstract S getData(T var1);

    protected abstract void render(T var1, S var2, MatrixStack var3, VertexConsumerProvider var4, int var5, float var6);

    @Environment(value=EnvType.CLIENT)
    public static class TextDisplayEntityRenderer
    extends DisplayEntityRenderer<DisplayEntity.TextDisplayEntity, DisplayEntity.TextDisplayEntity.Data> {
        private final TextRenderer displayTextRenderer;

        protected TextDisplayEntityRenderer(EntityRendererFactory.Context context) {
            super(context);
            this.displayTextRenderer = context.getTextRenderer();
        }

        private DisplayEntity.TextDisplayEntity.TextLines getLines(Text text, int width) {
            List<OrderedText> list = this.displayTextRenderer.wrapLines(text, width);
            ArrayList<DisplayEntity.TextDisplayEntity.TextLine> list2 = new ArrayList<DisplayEntity.TextDisplayEntity.TextLine>(list.size());
            int i = 0;
            for (OrderedText orderedText : list) {
                int j = this.displayTextRenderer.getWidth(orderedText);
                i = Math.max(i, j);
                list2.add(new DisplayEntity.TextDisplayEntity.TextLine(orderedText, j));
            }
            return new DisplayEntity.TextDisplayEntity.TextLines(list2, i);
        }

        @Override
        @Nullable
        protected DisplayEntity.TextDisplayEntity.Data getData(DisplayEntity.TextDisplayEntity textDisplayEntity) {
            return textDisplayEntity.getData();
        }

        @Override
        public void render(DisplayEntity.TextDisplayEntity textDisplayEntity, DisplayEntity.TextDisplayEntity.Data data, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, float f) {
            int j;
            float g;
            byte b = data.flags();
            boolean bl = (b & DisplayEntity.TextDisplayEntity.SEE_THROUGH_FLAG) != 0;
            boolean bl2 = (b & DisplayEntity.TextDisplayEntity.DEFAULT_BACKGROUND_FLAG) != 0;
            boolean bl3 = (b & DisplayEntity.TextDisplayEntity.SHADOW_FLAG) != 0;
            DisplayEntity.TextDisplayEntity.TextAlignment textAlignment = DisplayEntity.TextDisplayEntity.getAlignment(b);
            byte c = (byte)data.textOpacity().lerp(f);
            if (bl2) {
                g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
                j = (int)(g * 255.0f) << 24;
            } else {
                j = data.backgroundColor().lerp(f);
            }
            g = 0.0f;
            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
            matrix4f.rotate((float)Math.PI, 0.0f, 1.0f, 0.0f);
            matrix4f.scale(-0.025f, -0.025f, -0.025f);
            DisplayEntity.TextDisplayEntity.TextLines textLines = textDisplayEntity.splitLines(this::getLines);
            int k = this.displayTextRenderer.fontHeight + 1;
            int l = textLines.width();
            int m = textLines.lines().size() * k;
            matrix4f.translate(1.0f - (float)l / 2.0f, -m, 0.0f);
            if (j != 0) {
                VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(bl ? RenderLayer.getTextBackgroundSeeThrough() : RenderLayer.getTextBackground());
                vertexConsumer.vertex(matrix4f, -1.0f, -1.0f, 0.0f).color(j).light(i);
                vertexConsumer.vertex(matrix4f, -1.0f, (float)m, 0.0f).color(j).light(i);
                vertexConsumer.vertex(matrix4f, (float)l, (float)m, 0.0f).color(j).light(i);
                vertexConsumer.vertex(matrix4f, (float)l, -1.0f, 0.0f).color(j).light(i);
            }
            for (DisplayEntity.TextDisplayEntity.TextLine textLine : textLines.lines()) {
                float h = switch (textAlignment) {
                    default -> throw new MatchException(null, null);
                    case DisplayEntity.TextDisplayEntity.TextAlignment.LEFT -> 0.0f;
                    case DisplayEntity.TextDisplayEntity.TextAlignment.RIGHT -> l - textLine.width();
                    case DisplayEntity.TextDisplayEntity.TextAlignment.CENTER -> (float)l / 2.0f - (float)textLine.width() / 2.0f;
                };
                this.displayTextRenderer.draw(textLine.contents(), h, g, c << 24 | 0xFFFFFF, bl3, matrix4f, vertexConsumerProvider, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.POLYGON_OFFSET, 0, i);
                g += (float)k;
            }
        }

        @Override
        @Nullable
        protected /* synthetic */ Object getData(DisplayEntity entity) {
            return this.getData((DisplayEntity.TextDisplayEntity)entity);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ItemDisplayEntityRenderer
    extends DisplayEntityRenderer<DisplayEntity.ItemDisplayEntity, DisplayEntity.ItemDisplayEntity.Data> {
        private final ItemRenderer itemRenderer;

        protected ItemDisplayEntityRenderer(EntityRendererFactory.Context context) {
            super(context);
            this.itemRenderer = context.getItemRenderer();
        }

        @Override
        @Nullable
        protected DisplayEntity.ItemDisplayEntity.Data getData(DisplayEntity.ItemDisplayEntity itemDisplayEntity) {
            return itemDisplayEntity.getData();
        }

        @Override
        public void render(DisplayEntity.ItemDisplayEntity itemDisplayEntity, DisplayEntity.ItemDisplayEntity.Data data, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, float f) {
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float)Math.PI));
            this.itemRenderer.renderItem(data.itemStack(), data.itemTransform(), i, OverlayTexture.DEFAULT_UV, matrixStack, vertexConsumerProvider, itemDisplayEntity.getWorld(), itemDisplayEntity.getId());
        }

        @Override
        @Nullable
        protected /* synthetic */ Object getData(DisplayEntity entity) {
            return this.getData((DisplayEntity.ItemDisplayEntity)entity);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class BlockDisplayEntityRenderer
    extends DisplayEntityRenderer<DisplayEntity.BlockDisplayEntity, DisplayEntity.BlockDisplayEntity.Data> {
        private final BlockRenderManager blockRenderManager;

        protected BlockDisplayEntityRenderer(EntityRendererFactory.Context context) {
            super(context);
            this.blockRenderManager = context.getBlockRenderManager();
        }

        @Override
        @Nullable
        protected DisplayEntity.BlockDisplayEntity.Data getData(DisplayEntity.BlockDisplayEntity blockDisplayEntity) {
            return blockDisplayEntity.getData();
        }

        @Override
        public void render(DisplayEntity.BlockDisplayEntity blockDisplayEntity, DisplayEntity.BlockDisplayEntity.Data data, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, float f) {
            this.blockRenderManager.renderBlockAsEntity(data.blockState(), matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV);
        }

        @Override
        @Nullable
        protected /* synthetic */ Object getData(DisplayEntity entity) {
            return this.getData((DisplayEntity.BlockDisplayEntity)entity);
        }
    }
}

