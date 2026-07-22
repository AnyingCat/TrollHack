/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TranslucentBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;

@Environment(value=EnvType.CLIENT)
public class FluidRenderer {
    private static final float field_32781 = 0.8888889f;
    private final Sprite[] lavaSprites = new Sprite[2];
    private final Sprite[] waterSprites = new Sprite[2];
    private Sprite waterOverlaySprite;

    protected void onResourceReload() {
        this.lavaSprites[0] = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.LAVA.getDefaultState()).getParticleSprite();
        this.lavaSprites[1] = ModelLoader.LAVA_FLOW.getSprite();
        this.waterSprites[0] = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.WATER.getDefaultState()).getParticleSprite();
        this.waterSprites[1] = ModelLoader.WATER_FLOW.getSprite();
        this.waterOverlaySprite = ModelLoader.WATER_OVERLAY.getSprite();
    }

    private static boolean isSameFluid(FluidState a, FluidState b) {
        return b.getFluid().matchesType(a.getFluid());
    }

    private static boolean isSideCovered(BlockView world, Direction direction, float height, BlockPos pos, BlockState state) {
        if (state.isOpaque()) {
            VoxelShape voxelShape = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, height, 1.0);
            VoxelShape voxelShape2 = state.getCullingShape(world, pos);
            return VoxelShapes.isSideCovered(voxelShape, voxelShape2, direction);
        }
        return false;
    }

    private static boolean isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation, BlockState state) {
        return FluidRenderer.isSideCovered(world, direction, maxDeviation, pos.offset(direction), state);
    }

    private static boolean isOppositeSideCovered(BlockView world, BlockPos pos, BlockState state, Direction direction) {
        return FluidRenderer.isSideCovered(world, direction.getOpposite(), 1.0f, pos, state);
    }

    public static boolean shouldRenderSide(BlockRenderView world, BlockPos pos, FluidState fluidState, BlockState blockState, Direction direction, FluidState neighborFluidState) {
        return !FluidRenderer.isOppositeSideCovered(world, pos, blockState, direction) && !FluidRenderer.isSameFluid(fluidState, neighborFluidState);
    }

    public void render(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        float am;
        float al;
        float ai;
        float ae;
        float ad;
        float ac;
        float ab;
        float aa;
        float z;
        float x;
        float w;
        float v;
        float u;
        float t;
        float s;
        float r;
        float q;
        float p;
        float o;
        boolean bl = fluidState.isIn(FluidTags.LAVA);
        Sprite[] sprites = bl ? this.lavaSprites : this.waterSprites;
        int i = bl ? 0xFFFFFF : BiomeColors.getWaterColor(world, pos);
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        BlockState blockState2 = world.getBlockState(pos.offset(Direction.DOWN));
        FluidState fluidState2 = blockState2.getFluidState();
        BlockState blockState3 = world.getBlockState(pos.offset(Direction.UP));
        FluidState fluidState3 = blockState3.getFluidState();
        BlockState blockState4 = world.getBlockState(pos.offset(Direction.NORTH));
        FluidState fluidState4 = blockState4.getFluidState();
        BlockState blockState5 = world.getBlockState(pos.offset(Direction.SOUTH));
        FluidState fluidState5 = blockState5.getFluidState();
        BlockState blockState6 = world.getBlockState(pos.offset(Direction.WEST));
        FluidState fluidState6 = blockState6.getFluidState();
        BlockState blockState7 = world.getBlockState(pos.offset(Direction.EAST));
        FluidState fluidState7 = blockState7.getFluidState();
        boolean bl2 = !FluidRenderer.isSameFluid(fluidState, fluidState3);
        boolean bl3 = FluidRenderer.shouldRenderSide(world, pos, fluidState, blockState, Direction.DOWN, fluidState2) && !FluidRenderer.isSideCovered((BlockView)world, pos, Direction.DOWN, 0.8888889f, blockState2);
        boolean bl4 = FluidRenderer.shouldRenderSide(world, pos, fluidState, blockState, Direction.NORTH, fluidState4);
        boolean bl5 = FluidRenderer.shouldRenderSide(world, pos, fluidState, blockState, Direction.SOUTH, fluidState5);
        boolean bl6 = FluidRenderer.shouldRenderSide(world, pos, fluidState, blockState, Direction.WEST, fluidState6);
        boolean bl7 = FluidRenderer.shouldRenderSide(world, pos, fluidState, blockState, Direction.EAST, fluidState7);
        if (!(bl2 || bl3 || bl7 || bl6 || bl4 || bl5)) {
            return;
        }
        float j = world.getBrightness(Direction.DOWN, true);
        float k = world.getBrightness(Direction.UP, true);
        float l = world.getBrightness(Direction.NORTH, true);
        float m = world.getBrightness(Direction.WEST, true);
        Fluid fluid = fluidState.getFluid();
        float n = this.getFluidHeight(world, fluid, pos, blockState, fluidState);
        if (n >= 1.0f) {
            o = 1.0f;
            p = 1.0f;
            q = 1.0f;
            r = 1.0f;
        } else {
            s = this.getFluidHeight(world, fluid, pos.north(), blockState4, fluidState4);
            t = this.getFluidHeight(world, fluid, pos.south(), blockState5, fluidState5);
            u = this.getFluidHeight(world, fluid, pos.east(), blockState7, fluidState7);
            v = this.getFluidHeight(world, fluid, pos.west(), blockState6, fluidState6);
            o = this.calculateFluidHeight(world, fluid, n, s, u, pos.offset(Direction.NORTH).offset(Direction.EAST));
            p = this.calculateFluidHeight(world, fluid, n, s, v, pos.offset(Direction.NORTH).offset(Direction.WEST));
            q = this.calculateFluidHeight(world, fluid, n, t, u, pos.offset(Direction.SOUTH).offset(Direction.EAST));
            r = this.calculateFluidHeight(world, fluid, n, t, v, pos.offset(Direction.SOUTH).offset(Direction.WEST));
        }
        s = pos.getX() & 0xF;
        t = pos.getY() & 0xF;
        u = pos.getZ() & 0xF;
        v = 0.001f;
        float f2 = w = bl3 ? 0.001f : 0.0f;
        if (bl2 && !FluidRenderer.isSideCovered((BlockView)world, pos, Direction.UP, Math.min(Math.min(p, r), Math.min(q, o)), blockState3)) {
            float ag;
            float af;
            float y;
            p -= 0.001f;
            r -= 0.001f;
            q -= 0.001f;
            o -= 0.001f;
            Vec3d vec3d = fluidState.getVelocity(world, pos);
            if (vec3d.x == 0.0 && vec3d.z == 0.0) {
                sprite = sprites[0];
                x = sprite.getFrameU(0.0f);
                y = sprite.getFrameV(0.0f);
                z = x;
                aa = sprite.getFrameV(1.0f);
                ab = sprite.getFrameU(1.0f);
                ac = aa;
                ad = ab;
                ae = y;
            } else {
                sprite = sprites[1];
                af = (float)MathHelper.atan2(vec3d.z, vec3d.x) - 1.5707964f;
                ag = MathHelper.sin(af) * 0.25f;
                float ah = MathHelper.cos(af) * 0.25f;
                ai = 0.5f;
                x = sprite.getFrameU(0.5f + (-ah - ag));
                y = sprite.getFrameV(0.5f + (-ah + ag));
                z = sprite.getFrameU(0.5f + (-ah + ag));
                aa = sprite.getFrameV(0.5f + (ah + ag));
                ab = sprite.getFrameU(0.5f + (ah + ag));
                ac = sprite.getFrameV(0.5f + (ah - ag));
                ad = sprite.getFrameU(0.5f + (ah - ag));
                ae = sprite.getFrameV(0.5f + (-ah - ag));
            }
            float aj = (x + z + ab + ad) / 4.0f;
            af = (y + aa + ac + ae) / 4.0f;
            ag = sprites[0].getAnimationFrameDelta();
            x = MathHelper.lerp(ag, x, aj);
            z = MathHelper.lerp(ag, z, aj);
            ab = MathHelper.lerp(ag, ab, aj);
            ad = MathHelper.lerp(ag, ad, aj);
            y = MathHelper.lerp(ag, y, af);
            aa = MathHelper.lerp(ag, aa, af);
            ac = MathHelper.lerp(ag, ac, af);
            ae = MathHelper.lerp(ag, ae, af);
            int ak = this.getLight(world, pos);
            ai = k * f;
            al = k * g;
            am = k * h;
            this.vertex(vertexConsumer, s + 0.0f, t + p, u + 0.0f, ai, al, am, x, y, ak);
            this.vertex(vertexConsumer, s + 0.0f, t + r, u + 1.0f, ai, al, am, z, aa, ak);
            this.vertex(vertexConsumer, s + 1.0f, t + q, u + 1.0f, ai, al, am, ab, ac, ak);
            this.vertex(vertexConsumer, s + 1.0f, t + o, u + 0.0f, ai, al, am, ad, ae, ak);
            if (fluidState.canFlowTo(world, pos.up())) {
                this.vertex(vertexConsumer, s + 0.0f, t + p, u + 0.0f, ai, al, am, x, y, ak);
                this.vertex(vertexConsumer, s + 1.0f, t + o, u + 0.0f, ai, al, am, ad, ae, ak);
                this.vertex(vertexConsumer, s + 1.0f, t + q, u + 1.0f, ai, al, am, ab, ac, ak);
                this.vertex(vertexConsumer, s + 0.0f, t + r, u + 1.0f, ai, al, am, z, aa, ak);
            }
        }
        if (bl3) {
            x = sprites[0].getMinU();
            z = sprites[0].getMaxU();
            ab = sprites[0].getMinV();
            ad = sprites[0].getMaxV();
            int an = this.getLight(world, pos.down());
            aa = j * f;
            ac = j * g;
            ae = j * h;
            this.vertex(vertexConsumer, s, t + w, u + 1.0f, aa, ac, ae, x, ad, an);
            this.vertex(vertexConsumer, s, t + w, u, aa, ac, ae, x, ab, an);
            this.vertex(vertexConsumer, s + 1.0f, t + w, u, aa, ac, ae, z, ab, an);
            this.vertex(vertexConsumer, s + 1.0f, t + w, u + 1.0f, aa, ac, ae, z, ad, an);
        }
        int ao = this.getLight(world, pos);
        for (Direction direction : Direction.Type.HORIZONTAL) {
            Block block;
            float ap;
            float y;
            if (!(switch (direction) {
                case Direction.NORTH -> {
                    ad = p;
                    y = o;
                    aa = s;
                    ae = s + 1.0f;
                    ac = u + 0.001f;
                    ap = u + 0.001f;
                    yield bl4;
                }
                case Direction.SOUTH -> {
                    ad = q;
                    y = r;
                    aa = s + 1.0f;
                    ae = s;
                    ac = u + 1.0f - 0.001f;
                    ap = u + 1.0f - 0.001f;
                    yield bl5;
                }
                case Direction.WEST -> {
                    ad = r;
                    y = p;
                    aa = s + 0.001f;
                    ae = s + 0.001f;
                    ac = u + 1.0f;
                    ap = u;
                    yield bl6;
                }
                default -> {
                    ad = o;
                    y = q;
                    aa = s + 1.0f - 0.001f;
                    ae = s + 1.0f - 0.001f;
                    ac = u;
                    ap = u + 1.0f;
                    yield bl7;
                }
            }) || FluidRenderer.isSideCovered((BlockView)world, pos, direction, Math.max(ad, y), world.getBlockState(pos.offset(direction)))) continue;
            BlockPos blockPos = pos.offset(direction);
            Sprite sprite2 = sprites[1];
            if (!bl && ((block = world.getBlockState(blockPos).getBlock()) instanceof TranslucentBlock || block instanceof LeavesBlock)) {
                sprite2 = this.waterOverlaySprite;
            }
            float ah = sprite2.getFrameU(0.0f);
            ai = sprite2.getFrameU(0.5f);
            al = sprite2.getFrameV((1.0f - ad) * 0.5f);
            am = sprite2.getFrameV((1.0f - y) * 0.5f);
            float aq = sprite2.getFrameV(0.5f);
            float ar = direction.getAxis() == Direction.Axis.Z ? l : m;
            float as = k * ar * f;
            float at = k * ar * g;
            float au = k * ar * h;
            this.vertex(vertexConsumer, aa, t + ad, ac, as, at, au, ah, al, ao);
            this.vertex(vertexConsumer, ae, t + y, ap, as, at, au, ai, am, ao);
            this.vertex(vertexConsumer, ae, t + w, ap, as, at, au, ai, aq, ao);
            this.vertex(vertexConsumer, aa, t + w, ac, as, at, au, ah, aq, ao);
            if (sprite2 == this.waterOverlaySprite) continue;
            this.vertex(vertexConsumer, aa, t + w, ac, as, at, au, ah, aq, ao);
            this.vertex(vertexConsumer, ae, t + w, ap, as, at, au, ai, aq, ao);
            this.vertex(vertexConsumer, ae, t + y, ap, as, at, au, ai, am, ao);
            this.vertex(vertexConsumer, aa, t + ad, ac, as, at, au, ah, al, ao);
        }
    }

    private float calculateFluidHeight(BlockRenderView world, Fluid fluid, float originHeight, float northSouthHeight, float eastWestHeight, BlockPos pos) {
        if (eastWestHeight >= 1.0f || northSouthHeight >= 1.0f) {
            return 1.0f;
        }
        float[] fs = new float[2];
        if (eastWestHeight > 0.0f || northSouthHeight > 0.0f) {
            float f = this.getFluidHeight(world, fluid, pos);
            if (f >= 1.0f) {
                return 1.0f;
            }
            this.addHeight(fs, f);
        }
        this.addHeight(fs, originHeight);
        this.addHeight(fs, eastWestHeight);
        this.addHeight(fs, northSouthHeight);
        return fs[0] / fs[1];
    }

    private void addHeight(float[] weightedAverageHeight, float height) {
        if (height >= 0.8f) {
            weightedAverageHeight[0] = weightedAverageHeight[0] + height * 10.0f;
            weightedAverageHeight[1] = weightedAverageHeight[1] + 10.0f;
        } else if (height >= 0.0f) {
            weightedAverageHeight[0] = weightedAverageHeight[0] + height;
            weightedAverageHeight[1] = weightedAverageHeight[1] + 1.0f;
        }
    }

    private float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return this.getFluidHeight(world, fluid, pos, blockState, blockState.getFluidState());
    }

    private float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (fluid.matchesType(fluidState.getFluid())) {
            BlockState blockState2 = world.getBlockState(pos.up());
            if (fluid.matchesType(blockState2.getFluidState().getFluid())) {
                return 1.0f;
            }
            return fluidState.getHeight();
        }
        if (!blockState.isSolid()) {
            return 0.0f;
        }
        return -1.0f;
    }

    private void vertex(VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, int n) {
        vertexConsumer.vertex(f, g, h).color(i, j, k, 1.0f).texture(l, m).light(n).normal(0.0f, 1.0f, 0.0f);
    }

    private int getLight(BlockRenderView world, BlockPos pos) {
        int i = WorldRenderer.getLightmapCoordinates(world, pos);
        int j = WorldRenderer.getLightmapCoordinates(world, pos.up());
        int k = i & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        int l = j & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        int m = i >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        int n = j >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        return (k > l ? k : l) | (m > n ? m : n) << 16;
    }
}

