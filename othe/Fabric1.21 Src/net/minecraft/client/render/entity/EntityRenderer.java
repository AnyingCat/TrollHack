/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.Leashable;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
    protected static final float field_32921 = 0.025f;
    public static final int field_52257 = 24;
    protected final EntityRenderDispatcher dispatcher;
    private final TextRenderer textRenderer;
    protected float shadowRadius;
    protected float shadowOpacity = 1.0f;

    protected EntityRenderer(EntityRendererFactory.Context ctx) {
        this.dispatcher = ctx.getRenderDispatcher();
        this.textRenderer = ctx.getTextRenderer();
    }

    public final int getLight(T entity, float tickDelta) {
        BlockPos blockPos = BlockPos.ofFloored(((Entity)entity).getClientCameraPosVec(tickDelta));
        return LightmapTextureManager.pack(this.getBlockLight(entity, blockPos), this.getSkyLight(entity, blockPos));
    }

    protected int getSkyLight(T entity, BlockPos pos) {
        return ((Entity)entity).getWorld().getLightLevel(LightType.SKY, pos);
    }

    protected int getBlockLight(T entity, BlockPos pos) {
        if (((Entity)entity).isOnFire()) {
            return 15;
        }
        return ((Entity)entity).getWorld().getLightLevel(LightType.BLOCK, pos);
    }

    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        Leashable leashable;
        Entity entity2;
        if (!((Entity)entity).shouldRender(x, y, z)) {
            return false;
        }
        if (((Entity)entity).ignoreCameraFrustum) {
            return true;
        }
        Box box = ((Entity)entity).getVisibilityBoundingBox().expand(0.5);
        if (box.isNaN() || box.getAverageSideLength() == 0.0) {
            box = new Box(((Entity)entity).getX() - 2.0, ((Entity)entity).getY() - 2.0, ((Entity)entity).getZ() - 2.0, ((Entity)entity).getX() + 2.0, ((Entity)entity).getY() + 2.0, ((Entity)entity).getZ() + 2.0);
        }
        if (frustum.isVisible(box)) {
            return true;
        }
        if (entity instanceof Leashable && (entity2 = (leashable = (Leashable)entity).getLeashHolder()) != null) {
            return frustum.isVisible(entity2.getVisibilityBoundingBox());
        }
        return false;
    }

    public Vec3d getPositionOffset(T entity, float tickDelta) {
        return Vec3d.ZERO;
    }

    public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        Leashable leashable;
        Entity entity2;
        if (entity instanceof Leashable && (entity2 = (leashable = (Leashable)entity).getLeashHolder()) != null) {
            this.renderLeash(entity, tickDelta, matrices, vertexConsumers, entity2);
        }
        if (!this.hasLabel(entity)) {
            return;
        }
        this.renderLabelIfPresent(entity, ((Entity)entity).getDisplayName(), matrices, vertexConsumers, light, tickDelta);
    }

    private <E extends Entity> void renderLeash(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, E leashHolder) {
        int u;
        matrices.push();
        Vec3d vec3d = leashHolder.getLeashPos(tickDelta);
        double d = (double)(((Entity)entity).lerpYaw(tickDelta) * ((float)Math.PI / 180)) + 1.5707963267948966;
        Vec3d vec3d2 = ((Entity)entity).getLeashOffset(tickDelta);
        double e = Math.cos(d) * vec3d2.z + Math.sin(d) * vec3d2.x;
        double f = Math.sin(d) * vec3d2.z - Math.cos(d) * vec3d2.x;
        double g = MathHelper.lerp((double)tickDelta, ((Entity)entity).prevX, ((Entity)entity).getX()) + e;
        double h = MathHelper.lerp((double)tickDelta, ((Entity)entity).prevY, ((Entity)entity).getY()) + vec3d2.y;
        double i = MathHelper.lerp((double)tickDelta, ((Entity)entity).prevZ, ((Entity)entity).getZ()) + f;
        matrices.translate(e, vec3d2.y, f);
        float j = (float)(vec3d.x - g);
        float k = (float)(vec3d.y - h);
        float l = (float)(vec3d.z - i);
        float m = 0.025f;
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLeash());
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float n = MathHelper.inverseSqrt(j * j + l * l) * 0.025f / 2.0f;
        float o = l * n;
        float p = j * n;
        BlockPos blockPos = BlockPos.ofFloored(((Entity)entity).getCameraPosVec(tickDelta));
        BlockPos blockPos2 = BlockPos.ofFloored(leashHolder.getCameraPosVec(tickDelta));
        int q = this.getBlockLight(entity, blockPos);
        int r = this.dispatcher.getRenderer(leashHolder).getBlockLight(leashHolder, blockPos2);
        int s = ((Entity)entity).getWorld().getLightLevel(LightType.SKY, blockPos);
        int t = ((Entity)entity).getWorld().getLightLevel(LightType.SKY, blockPos2);
        for (u = 0; u <= 24; ++u) {
            EntityRenderer.renderLeashSegment(vertexConsumer, matrix4f, j, k, l, q, r, s, t, 0.025f, 0.025f, o, p, u, false);
        }
        for (u = 24; u >= 0; --u) {
            EntityRenderer.renderLeashSegment(vertexConsumer, matrix4f, j, k, l, q, r, s, t, 0.025f, 0.0f, o, p, u, true);
        }
        matrices.pop();
    }

    private static void renderLeashSegment(VertexConsumer vertexConsumer, Matrix4f matrix, float leashedEntityX, float leashedEntityY, float leashedEntityZ, int leashedEntityBlockLight, int leashHolderBlockLight, int leashedEntitySkyLight, int leashHolderSkyLight, float f, float g, float h, float i, int segmentIndex, boolean isLeashKnot) {
        float j = (float)segmentIndex / 24.0f;
        int k = (int)MathHelper.lerp(j, (float)leashedEntityBlockLight, (float)leashHolderBlockLight);
        int l = (int)MathHelper.lerp(j, (float)leashedEntitySkyLight, (float)leashHolderSkyLight);
        int m = LightmapTextureManager.pack(k, l);
        float n = segmentIndex % 2 == (isLeashKnot ? 1 : 0) ? 0.7f : 1.0f;
        float o = 0.5f * n;
        float p = 0.4f * n;
        float q = 0.3f * n;
        float r = leashedEntityX * j;
        float s = leashedEntityY > 0.0f ? leashedEntityY * j * j : leashedEntityY - leashedEntityY * (1.0f - j) * (1.0f - j);
        float t = leashedEntityZ * j;
        vertexConsumer.vertex(matrix, r - h, s + g, t + i).color(o, p, q, 1.0f).light(m);
        vertexConsumer.vertex(matrix, r + h, s + f - g, t - i).color(o, p, q, 1.0f).light(m);
    }

    /**
     * Determines whether the passed entity should render with a nameplate above its head.
     * 
     * <p>Checks for a custom nametag on living entities, and for teams/team visibilities for players.
     */
    protected boolean hasLabel(T entity) {
        return ((Entity)entity).shouldRenderName() || ((Entity)entity).hasCustomName() && entity == this.dispatcher.targetedEntity;
    }

    public abstract Identifier getTexture(T var1);

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    protected void renderLabelIfPresent(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        double d = this.dispatcher.getSquaredDistanceToCamera((Entity)entity);
        if (d > 4096.0) {
            return;
        }
        Vec3d vec3d = ((Entity)entity).getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, ((Entity)entity).getYaw(tickDelta));
        if (vec3d == null) {
            return;
        }
        boolean bl = !((Entity)entity).isSneaky();
        int i = "deadmau5".equals(text.getString()) ? -10 : 0;
        matrices.push();
        matrices.translate(vec3d.x, vec3d.y + 0.5, vec3d.z);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float f = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
        int j = (int)(f * 255.0f) << 24;
        TextRenderer textRenderer = this.getTextRenderer();
        float g = -textRenderer.getWidth(text) / 2;
        textRenderer.draw(text, g, (float)i, 0x20FFFFFF, false, matrix4f, vertexConsumers, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, j, light);
        if (bl) {
            textRenderer.draw(text, g, (float)i, Colors.WHITE, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        }
        matrices.pop();
    }

    protected float getShadowRadius(T entity) {
        return this.shadowRadius;
    }
}

