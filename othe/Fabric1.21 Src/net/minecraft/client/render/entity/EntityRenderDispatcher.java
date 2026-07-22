/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class EntityRenderDispatcher
implements SynchronousResourceReloader {
    private static final RenderLayer SHADOW_LAYER = RenderLayer.getEntityShadow(Identifier.ofVanilla("textures/misc/shadow.png"));
    private static final float field_43377 = 32.0f;
    private static final float field_43378 = 0.5f;
    private Map<EntityType<?>, EntityRenderer<?>> renderers = ImmutableMap.of();
    private Map<SkinTextures.Model, EntityRenderer<? extends PlayerEntity>> modelRenderers = Map.of();
    public final TextureManager textureManager;
    private World world;
    public Camera camera;
    private Quaternionf rotation;
    public Entity targetedEntity;
    private final ItemRenderer itemRenderer;
    private final BlockRenderManager blockRenderManager;
    private final HeldItemRenderer heldItemRenderer;
    private final TextRenderer textRenderer;
    public final GameOptions gameOptions;
    private final EntityModelLoader modelLoader;
    private boolean renderShadows = true;
    private boolean renderHitboxes;

    public <E extends Entity> int getLight(E entity, float tickDelta) {
        return this.getRenderer(entity).getLight(entity, tickDelta);
    }

    public EntityRenderDispatcher(MinecraftClient client, TextureManager textureManager, ItemRenderer itemRenderer, BlockRenderManager blockRenderManager, TextRenderer textRenderer, GameOptions gameOptions, EntityModelLoader modelLoader) {
        this.textureManager = textureManager;
        this.itemRenderer = itemRenderer;
        this.heldItemRenderer = new HeldItemRenderer(client, this, itemRenderer);
        this.blockRenderManager = blockRenderManager;
        this.textRenderer = textRenderer;
        this.gameOptions = gameOptions;
        this.modelLoader = modelLoader;
    }

    public <T extends Entity> EntityRenderer<? super T> getRenderer(T entity) {
        if (entity instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity abstractClientPlayerEntity = (AbstractClientPlayerEntity)entity;
            SkinTextures.Model model = abstractClientPlayerEntity.getSkinTextures().model();
            EntityRenderer<? extends PlayerEntity> entityRenderer = this.modelRenderers.get((Object)model);
            if (entityRenderer != null) {
                return entityRenderer;
            }
            return this.modelRenderers.get((Object)SkinTextures.Model.WIDE);
        }
        return this.renderers.get(entity.getType());
    }

    public void configure(World world, Camera camera, Entity target) {
        this.world = world;
        this.camera = camera;
        this.rotation = camera.getRotation();
        this.targetedEntity = target;
    }

    public void setRotation(Quaternionf rotation) {
        this.rotation = rotation;
    }

    public void setRenderShadows(boolean renderShadows) {
        this.renderShadows = renderShadows;
    }

    public void setRenderHitboxes(boolean renderHitboxes) {
        this.renderHitboxes = renderHitboxes;
    }

    public boolean shouldRenderHitboxes() {
        return this.renderHitboxes;
    }

    public <E extends Entity> boolean shouldRender(E entity, Frustum frustum, double x, double y, double z) {
        EntityRenderer<E> entityRenderer = this.getRenderer(entity);
        return entityRenderer.shouldRender(entity, frustum, x, y, z);
    }

    public <E extends Entity> void render(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        EntityRenderer<E> entityRenderer = this.getRenderer(entity);
        try {
            double h;
            float i;
            float g;
            Vec3d vec3d = entityRenderer.getPositionOffset(entity, tickDelta);
            double d = x + vec3d.getX();
            double e = y + vec3d.getY();
            double f = z + vec3d.getZ();
            matrices.push();
            matrices.translate(d, e, f);
            entityRenderer.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
            if (entity.doesRenderOnFire()) {
                this.renderFire(matrices, vertexConsumers, entity, MathHelper.rotateAround(MathHelper.Y_AXIS, this.rotation, new Quaternionf()));
            }
            matrices.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ());
            if (this.gameOptions.getEntityShadows().getValue().booleanValue() && this.renderShadows && !entity.isInvisible() && (g = entityRenderer.getShadowRadius(entity)) > 0.0f && (i = (float)((1.0 - (h = this.getSquaredDistanceToCamera(entity.getX(), entity.getY(), entity.getZ())) / 256.0) * (double)entityRenderer.shadowOpacity)) > 0.0f) {
                EntityRenderDispatcher.renderShadow(matrices, vertexConsumers, entity, i, tickDelta, this.world, Math.min(g, 32.0f));
            }
            if (this.renderHitboxes && !entity.isInvisible() && !MinecraftClient.getInstance().hasReducedDebugInfo()) {
                EntityRenderDispatcher.renderHitbox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), entity, tickDelta, 1.0f, 1.0f, 1.0f);
            }
            matrices.pop();
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, "Rendering entity in world");
            CrashReportSection crashReportSection = crashReport.addElement("Entity being rendered");
            entity.populateCrashReport(crashReportSection);
            CrashReportSection crashReportSection2 = crashReport.addElement("Renderer details");
            crashReportSection2.add("Assigned renderer", entityRenderer);
            crashReportSection2.add("Location", CrashReportSection.createPositionString((HeightLimitView)this.world, x, y, z));
            crashReportSection2.add("Rotation", Float.valueOf(yaw));
            crashReportSection2.add("Delta", Float.valueOf(tickDelta));
            throw new CrashException(crashReport);
        }
    }

    private static void renderServerSideHitbox(MatrixStack matrices, Entity entity, VertexConsumerProvider vertexConsumers) {
        Entity entity2 = EntityRenderDispatcher.getIntegratedServerEntity(entity);
        if (entity2 == null) {
            DebugRenderer.drawString(matrices, vertexConsumers, "Missing", entity.getX(), entity.getBoundingBox().maxY + 1.5, entity.getZ(), Colors.RED);
            return;
        }
        matrices.push();
        matrices.translate(entity2.getX() - entity.getX(), entity2.getY() - entity.getY(), entity2.getZ() - entity.getZ());
        EntityRenderDispatcher.renderHitbox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), entity2, 1.0f, 0.0f, 1.0f, 0.0f);
        EntityRenderDispatcher.drawVector(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), new Vector3f(), entity2.getVelocity(), -256);
        matrices.pop();
    }

    @Nullable
    private static Entity getIntegratedServerEntity(Entity entity) {
        ServerWorld serverWorld;
        IntegratedServer integratedServer = MinecraftClient.getInstance().getServer();
        if (integratedServer != null && (serverWorld = integratedServer.getWorld(entity.getWorld().getRegistryKey())) != null) {
            return serverWorld.getEntityById(entity.getId());
        }
        return null;
    }

    private static void renderHitbox(MatrixStack matrices, VertexConsumer vertices, Entity entity, float tickDelta, float red, float green, float blue) {
        Entity entity2;
        Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
        WorldRenderer.drawBox(matrices, vertices, box, red, green, blue, 1.0f);
        if (entity instanceof EnderDragonEntity) {
            double d = -MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
            double e = -MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
            double f = -MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
            for (EnderDragonPart enderDragonPart : ((EnderDragonEntity)entity).getBodyParts()) {
                matrices.push();
                double g = d + MathHelper.lerp((double)tickDelta, enderDragonPart.lastRenderX, enderDragonPart.getX());
                double h = e + MathHelper.lerp((double)tickDelta, enderDragonPart.lastRenderY, enderDragonPart.getY());
                double i = f + MathHelper.lerp((double)tickDelta, enderDragonPart.lastRenderZ, enderDragonPart.getZ());
                matrices.translate(g, h, i);
                WorldRenderer.drawBox(matrices, vertices, enderDragonPart.getBoundingBox().offset(-enderDragonPart.getX(), -enderDragonPart.getY(), -enderDragonPart.getZ()), 0.25f, 1.0f, 0.0f, 1.0f);
                matrices.pop();
            }
        }
        if (entity instanceof LivingEntity) {
            float j = 0.01f;
            WorldRenderer.drawBox(matrices, vertices, box.minX, entity.getStandingEyeHeight() - 0.01f, box.minZ, box.maxX, entity.getStandingEyeHeight() + 0.01f, box.maxZ, 1.0f, 0.0f, 0.0f, 1.0f);
        }
        if ((entity2 = entity.getVehicle()) != null) {
            float k = Math.min(entity2.getWidth(), entity.getWidth()) / 2.0f;
            float l = 0.0625f;
            Vec3d vec3d = entity2.getPassengerRidingPos(entity).subtract(entity.getPos());
            WorldRenderer.drawBox(matrices, vertices, vec3d.x - (double)k, vec3d.y, vec3d.z - (double)k, vec3d.x + (double)k, vec3d.y + 0.0625, vec3d.z + (double)k, 1.0f, 1.0f, 0.0f, 1.0f);
        }
        EntityRenderDispatcher.drawVector(matrices, vertices, new Vector3f(0.0f, entity.getStandingEyeHeight(), 0.0f), entity.getRotationVec(tickDelta).multiply(2.0), -16776961);
    }

    private static void drawVector(MatrixStack matrices, VertexConsumer vertexConsumers, Vector3f offset, Vec3d vec, int color) {
        MatrixStack.Entry entry = matrices.peek();
        vertexConsumers.vertex(entry, offset).color(color).normal(entry, (float)vec.x, (float)vec.y, (float)vec.z);
        vertexConsumers.vertex(entry, (float)((double)offset.x() + vec.x), (float)((double)offset.y() + vec.y), (float)((double)offset.z() + vec.z)).color(color).normal(entry, (float)vec.x, (float)vec.y, (float)vec.z);
    }

    private void renderFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, Quaternionf rotation) {
        Sprite sprite = ModelLoader.FIRE_0.getSprite();
        Sprite sprite2 = ModelLoader.FIRE_1.getSprite();
        matrices.push();
        float f = entity.getWidth() * 1.4f;
        matrices.scale(f, f, f);
        float g = 0.5f;
        float h = 0.0f;
        float i = entity.getHeight() / f;
        float j = 0.0f;
        matrices.multiply(rotation);
        matrices.translate(0.0f, 0.0f, 0.3f - (float)((int)i) * 0.02f);
        float k = 0.0f;
        int l = 0;
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        MatrixStack.Entry entry = matrices.peek();
        while (i > 0.0f) {
            Sprite sprite3 = l % 2 == 0 ? sprite : sprite2;
            float m = sprite3.getMinU();
            float n = sprite3.getMinV();
            float o = sprite3.getMaxU();
            float p = sprite3.getMaxV();
            if (l / 2 % 2 == 0) {
                float q = o;
                o = m;
                m = q;
            }
            EntityRenderDispatcher.drawFireVertex(entry, vertexConsumer, -g - 0.0f, 0.0f - j, k, o, p);
            EntityRenderDispatcher.drawFireVertex(entry, vertexConsumer, g - 0.0f, 0.0f - j, k, m, p);
            EntityRenderDispatcher.drawFireVertex(entry, vertexConsumer, g - 0.0f, 1.4f - j, k, m, n);
            EntityRenderDispatcher.drawFireVertex(entry, vertexConsumer, -g - 0.0f, 1.4f - j, k, o, n);
            i -= 0.45f;
            j -= 0.45f;
            g *= 0.9f;
            k -= 0.03f;
            ++l;
        }
        matrices.pop();
    }

    private static void drawFireVertex(MatrixStack.Entry entry, VertexConsumer vertices, float x, float y, float z, float u, float v) {
        vertices.vertex(entry, x, y, z).color(Colors.WHITE).texture(u, v).overlay(0, 10).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).normal(entry, 0.0f, 1.0f, 0.0f);
    }

    private static void renderShadow(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, float opacity, float tickDelta, WorldView world, float radius) {
        double d = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
        double e = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
        double f = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
        float g = Math.min(opacity / 0.5f, radius);
        int i = MathHelper.floor(d - (double)radius);
        int j = MathHelper.floor(d + (double)radius);
        int k = MathHelper.floor(e - (double)g);
        int l = MathHelper.floor(e);
        int m = MathHelper.floor(f - (double)radius);
        int n = MathHelper.floor(f + (double)radius);
        MatrixStack.Entry entry = matrices.peek();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(SHADOW_LAYER);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int o = m; o <= n; ++o) {
            for (int p = i; p <= j; ++p) {
                mutable.set(p, 0, o);
                Chunk chunk = world.getChunk(mutable);
                for (int q = k; q <= l; ++q) {
                    mutable.setY(q);
                    float h = opacity - (float)(e - (double)mutable.getY()) * 0.5f;
                    EntityRenderDispatcher.renderShadowPart(entry, vertexConsumer, chunk, world, mutable, d, e, f, radius, h);
                }
            }
        }
    }

    private static void renderShadowPart(MatrixStack.Entry entry, VertexConsumer vertices, Chunk chunk, WorldView world, BlockPos pos, double x, double y, double z, float radius, float opacity) {
        BlockPos blockPos = pos.down();
        BlockState blockState = chunk.getBlockState(blockPos);
        if (blockState.getRenderType() == BlockRenderType.INVISIBLE || world.getLightLevel(pos) <= 3) {
            return;
        }
        if (!blockState.isFullCube(chunk, blockPos)) {
            return;
        }
        VoxelShape voxelShape = blockState.getOutlineShape(chunk, blockPos);
        if (voxelShape.isEmpty()) {
            return;
        }
        float f = LightmapTextureManager.getBrightness(world.getDimension(), world.getLightLevel(pos));
        float g = opacity * 0.5f * f;
        if (g >= 0.0f) {
            if (g > 1.0f) {
                g = 1.0f;
            }
            int i = ColorHelper.Argb.getArgb(MathHelper.floor(g * 255.0f), 255, 255, 255);
            Box box = voxelShape.getBoundingBox();
            double d = (double)pos.getX() + box.minX;
            double e = (double)pos.getX() + box.maxX;
            double h = (double)pos.getY() + box.minY;
            double j = (double)pos.getZ() + box.minZ;
            double k = (double)pos.getZ() + box.maxZ;
            float l = (float)(d - x);
            float m = (float)(e - x);
            float n = (float)(h - y);
            float o = (float)(j - z);
            float p = (float)(k - z);
            float q = -l / 2.0f / radius + 0.5f;
            float r = -m / 2.0f / radius + 0.5f;
            float s = -o / 2.0f / radius + 0.5f;
            float t = -p / 2.0f / radius + 0.5f;
            EntityRenderDispatcher.drawShadowVertex(entry, vertices, i, l, n, o, q, s);
            EntityRenderDispatcher.drawShadowVertex(entry, vertices, i, l, n, p, q, t);
            EntityRenderDispatcher.drawShadowVertex(entry, vertices, i, m, n, p, r, t);
            EntityRenderDispatcher.drawShadowVertex(entry, vertices, i, m, n, o, r, s);
        }
    }

    private static void drawShadowVertex(MatrixStack.Entry entry, VertexConsumer vertices, int i, float x, float y, float z, float u, float v) {
        Vector3f vector3f = entry.getPositionMatrix().transformPosition(x, y, z, new Vector3f());
        vertices.vertex(vector3f.x(), vector3f.y(), vector3f.z(), i, u, v, OverlayTexture.DEFAULT_UV, 0xF000F0, 0.0f, 1.0f, 0.0f);
    }

    public void setWorld(@Nullable World world) {
        this.world = world;
        if (world == null) {
            this.camera = null;
        }
    }

    public double getSquaredDistanceToCamera(Entity entity) {
        return this.camera.getPos().squaredDistanceTo(entity.getPos());
    }

    public double getSquaredDistanceToCamera(double x, double y, double z) {
        return this.camera.getPos().squaredDistanceTo(x, y, z);
    }

    public Quaternionf getRotation() {
        return this.rotation;
    }

    public HeldItemRenderer getHeldItemRenderer() {
        return this.heldItemRenderer;
    }

    @Override
    public void reload(ResourceManager manager) {
        EntityRendererFactory.Context context = new EntityRendererFactory.Context(this, this.itemRenderer, this.blockRenderManager, this.heldItemRenderer, manager, this.modelLoader, this.textRenderer);
        this.renderers = EntityRenderers.reloadEntityRenderers(context);
        this.modelRenderers = EntityRenderers.reloadPlayerRenderers(context);
    }
}

