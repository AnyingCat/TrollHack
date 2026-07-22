/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

@Environment(value=EnvType.CLIENT)
public abstract class RenderPhase {
    private static final float VIEW_OFFSET_Z_LAYERING_SCALE = 0.99975586f;
    public static final double field_42230 = 8.0;
    protected final String name;
    private final Runnable beginAction;
    private final Runnable endAction;
    public static final Transparency NO_TRANSPARENCY = new Transparency("no_transparency", () -> RenderSystem.disableBlend(), () -> {});
    public static final Transparency ADDITIVE_TRANSPARENCY = new Transparency("additive_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    public static final Transparency LIGHTNING_TRANSPARENCY = new Transparency("lightning_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    public static final Transparency GLINT_TRANSPARENCY = new Transparency("glint_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_COLOR, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    public static final Transparency CRUMBLING_TRANSPARENCY = new Transparency("crumbling_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.DST_COLOR, GlStateManager.DstFactor.SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    public static final Transparency TRANSLUCENT_TRANSPARENCY = new Transparency("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    public static final ShaderProgram NO_PROGRAM = new ShaderProgram();
    public static final ShaderProgram POSITION_COLOR_LIGHTMAP_PROGRAM = new ShaderProgram(GameRenderer::getPositionColorLightmapProgram);
    public static final ShaderProgram POSITION_PROGRAM = new ShaderProgram(GameRenderer::getPositionProgram);
    public static final ShaderProgram POSITION_TEXTURE_PROGRAM = new ShaderProgram(GameRenderer::getPositionTexProgram);
    public static final ShaderProgram POSITION_COLOR_TEXTURE_LIGHTMAP_PROGRAM = new ShaderProgram(GameRenderer::getPositionColorTexLightmapProgram);
    public static final ShaderProgram COLOR_PROGRAM = new ShaderProgram(GameRenderer::getPositionColorProgram);
    public static final ShaderProgram SOLID_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeSolidProgram);
    public static final ShaderProgram CUTOUT_MIPPED_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeCutoutMippedProgram);
    public static final ShaderProgram CUTOUT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeCutoutProgram);
    public static final ShaderProgram TRANSLUCENT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTranslucentProgram);
    public static final ShaderProgram TRANSLUCENT_MOVING_BLOCK_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTranslucentMovingBlockProgram);
    public static final ShaderProgram ARMOR_CUTOUT_NO_CULL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeArmorCutoutNoCullProgram);
    public static final ShaderProgram ENTITY_SOLID_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntitySolidProgram);
    public static final ShaderProgram ENTITY_CUTOUT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityCutoutProgram);
    public static final ShaderProgram ENTITY_CUTOUT_NONULL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityCutoutNoNullProgram);
    public static final ShaderProgram ENTITY_CUTOUT_NONULL_OFFSET_Z_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityCutoutNoNullZOffsetProgram);
    public static final ShaderProgram ITEM_ENTITY_TRANSLUCENT_CULL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeItemEntityTranslucentCullProgram);
    public static final ShaderProgram ENTITY_TRANSLUCENT_CULL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityTranslucentCullProgram);
    public static final ShaderProgram ENTITY_TRANSLUCENT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityTranslucentProgram);
    public static final ShaderProgram ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityTranslucentEmissiveProgram);
    public static final ShaderProgram ENTITY_SMOOTH_CUTOUT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntitySmoothCutoutProgram);
    public static final ShaderProgram BEACON_BEAM_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeBeaconBeamProgram);
    public static final ShaderProgram ENTITY_DECAL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityDecalProgram);
    public static final ShaderProgram ENTITY_NO_OUTLINE_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityNoOutlineProgram);
    public static final ShaderProgram ENTITY_SHADOW_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityShadowProgram);
    public static final ShaderProgram ENTITY_ALPHA_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityAlphaProgram);
    public static final ShaderProgram EYES_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEyesProgram);
    public static final ShaderProgram ENERGY_SWIRL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEnergySwirlProgram);
    public static final ShaderProgram LEASH_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeLeashProgram);
    public static final ShaderProgram WATER_MASK_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeWaterMaskProgram);
    public static final ShaderProgram OUTLINE_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeOutlineProgram);
    public static final ShaderProgram ARMOR_ENTITY_GLINT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeArmorEntityGlintProgram);
    public static final ShaderProgram TRANSLUCENT_GLINT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeGlintTranslucentProgram);
    public static final ShaderProgram GLINT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeGlintProgram);
    public static final ShaderProgram ENTITY_GLINT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityGlintProgram);
    public static final ShaderProgram DIRECT_ENTITY_GLINT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityGlintDirectProgram);
    public static final ShaderProgram CRUMBLING_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeCrumblingProgram);
    public static final ShaderProgram TEXT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTextProgram);
    public static final ShaderProgram TEXT_BACKGROUND_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTextBackgroundProgram);
    public static final ShaderProgram TEXT_INTENSITY_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTextIntensityProgram);
    public static final ShaderProgram TRANSPARENT_TEXT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTextSeeThroughProgram);
    public static final ShaderProgram TRANSPARENT_TEXT_BACKGROUND_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTextBackgroundSeeThroughProgram);
    public static final ShaderProgram TRANSPARENT_TEXT_INTENSITY_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTextIntensitySeeThroughProgram);
    public static final ShaderProgram LIGHTNING_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeLightningProgram);
    public static final ShaderProgram TRIPWIRE_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTripwireProgram);
    public static final ShaderProgram END_PORTAL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEndPortalProgram);
    public static final ShaderProgram END_GATEWAY_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEndGatewayProgram);
    public static final ShaderProgram CLOUDS_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeCloudsProgram);
    public static final ShaderProgram LINES_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeLinesProgram);
    public static final ShaderProgram GUI_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeGuiProgram);
    public static final ShaderProgram GUI_OVERLAY_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeGuiOverlayProgram);
    public static final ShaderProgram GUI_TEXT_HIGHLIGHT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeGuiTextHighlightProgram);
    public static final ShaderProgram GUI_GHOST_RECIPE_OVERLAY_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeGuiGhostRecipeOverlayProgram);
    public static final ShaderProgram BREEZE_WIND_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeBreezeWindProgram);
    public static final Texture MIPMAP_BLOCK_ATLAS_TEXTURE = new Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, true);
    public static final Texture BLOCK_ATLAS_TEXTURE = new Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, false);
    public static final TextureBase NO_TEXTURE = new TextureBase();
    public static final Texturing DEFAULT_TEXTURING = new Texturing("default_texturing", () -> {}, () -> {});
    public static final Texturing GLINT_TEXTURING = new Texturing("glint_texturing", () -> RenderPhase.setupGlintTexturing(8.0f), () -> RenderSystem.resetTextureMatrix());
    public static final Texturing ENTITY_GLINT_TEXTURING = new Texturing("entity_glint_texturing", () -> RenderPhase.setupGlintTexturing(0.16f), () -> RenderSystem.resetTextureMatrix());
    public static final Lightmap ENABLE_LIGHTMAP = new Lightmap(true);
    public static final Lightmap DISABLE_LIGHTMAP = new Lightmap(false);
    public static final Overlay ENABLE_OVERLAY_COLOR = new Overlay(true);
    public static final Overlay DISABLE_OVERLAY_COLOR = new Overlay(false);
    public static final Cull ENABLE_CULLING = new Cull(true);
    public static final Cull DISABLE_CULLING = new Cull(false);
    public static final DepthTest ALWAYS_DEPTH_TEST = new DepthTest("always", 519);
    public static final DepthTest EQUAL_DEPTH_TEST = new DepthTest("==", 514);
    public static final DepthTest LEQUAL_DEPTH_TEST = new DepthTest("<=", 515);
    public static final DepthTest BIGGER_DEPTH_TEST = new DepthTest(">", 516);
    public static final WriteMaskState ALL_MASK = new WriteMaskState(true, true);
    public static final WriteMaskState COLOR_MASK = new WriteMaskState(true, false);
    public static final WriteMaskState DEPTH_MASK = new WriteMaskState(false, true);
    public static final Layering NO_LAYERING = new Layering("no_layering", () -> {}, () -> {});
    public static final Layering POLYGON_OFFSET_LAYERING = new Layering("polygon_offset_layering", () -> {
        RenderSystem.polygonOffset(-1.0f, -10.0f);
        RenderSystem.enablePolygonOffset();
    }, () -> {
        RenderSystem.polygonOffset(0.0f, 0.0f);
        RenderSystem.disablePolygonOffset();
    });
    public static final Layering VIEW_OFFSET_Z_LAYERING = new Layering("view_offset_z_layering", () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.scale(0.99975586f, 0.99975586f, 0.99975586f);
        RenderSystem.applyModelViewMatrix();
    }, () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    });
    public static final Target MAIN_TARGET = new Target("main_target", () -> {}, () -> {});
    public static final Target OUTLINE_TARGET = new Target("outline_target", () -> MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer().beginWrite(false), () -> MinecraftClient.getInstance().getFramebuffer().beginWrite(false));
    public static final Target TRANSLUCENT_TARGET = new Target("translucent_target", () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getTranslucentFramebuffer().beginWrite(false);
        }
    }, () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        }
    });
    public static final Target PARTICLES_TARGET = new Target("particles_target", () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getParticlesFramebuffer().beginWrite(false);
        }
    }, () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        }
    });
    public static final Target WEATHER_TARGET = new Target("weather_target", () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getWeatherFramebuffer().beginWrite(false);
        }
    }, () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        }
    });
    public static final Target CLOUDS_TARGET = new Target("clouds_target", () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getCloudsFramebuffer().beginWrite(false);
        }
    }, () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        }
    });
    public static final Target ITEM_ENTITY_TARGET = new Target("item_entity_target", () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getEntityFramebuffer().beginWrite(false);
        }
    }, () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        }
    });
    public static final LineWidth FULL_LINE_WIDTH = new LineWidth(OptionalDouble.of(1.0));
    public static final ColorLogic NO_COLOR_LOGIC = new ColorLogic("no_color_logic", () -> RenderSystem.disableColorLogicOp(), () -> {});
    public static final ColorLogic OR_REVERSE = new ColorLogic("or_reverse", () -> {
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
    }, () -> RenderSystem.disableColorLogicOp());

    public RenderPhase(String name, Runnable beginAction, Runnable endAction) {
        this.name = name;
        this.beginAction = beginAction;
        this.endAction = endAction;
    }

    public void startDrawing() {
        this.beginAction.run();
    }

    public void endDrawing() {
        this.endAction.run();
    }

    public String toString() {
        return this.name;
    }

    private static void setupGlintTexturing(float scale) {
        long l = (long)((double)Util.getMeasuringTimeMs() * MinecraftClient.getInstance().options.getGlintSpeed().getValue() * 8.0);
        float f = (float)(l % 110000L) / 110000.0f;
        float g = (float)(l % 30000L) / 30000.0f;
        Matrix4f matrix4f = new Matrix4f().translation(-f, g, 0.0f);
        matrix4f.rotateZ(0.17453292f).scale(scale);
        RenderSystem.setTextureMatrix(matrix4f);
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Transparency
    extends RenderPhase {
        public Transparency(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class ShaderProgram
    extends RenderPhase {
        private final Optional<Supplier<net.minecraft.client.gl.ShaderProgram>> supplier;

        public ShaderProgram(Supplier<net.minecraft.client.gl.ShaderProgram> supplier) {
            super("shader", () -> RenderSystem.setShader(supplier), () -> {});
            this.supplier = Optional.of(supplier);
        }

        public ShaderProgram() {
            super("shader", () -> RenderSystem.setShader(() -> null), () -> {});
            this.supplier = Optional.empty();
        }

        @Override
        public String toString() {
            return this.name + "[" + String.valueOf(this.supplier) + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Texture
    extends TextureBase {
        private final Optional<Identifier> id;
        private final boolean blur;
        private final boolean mipmap;

        public Texture(Identifier id, boolean blur, boolean mipmap) {
            super(() -> {
                TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                textureManager.getTexture(id).setFilter(blur, mipmap);
                RenderSystem.setShaderTexture(0, id);
            }, () -> {});
            this.id = Optional.of(id);
            this.blur = blur;
            this.mipmap = mipmap;
        }

        @Override
        public String toString() {
            return this.name + "[" + String.valueOf(this.id) + "(blur=" + this.blur + ", mipmap=" + this.mipmap + ")]";
        }

        @Override
        protected Optional<Identifier> getId() {
            return this.id;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class TextureBase
    extends RenderPhase {
        public TextureBase(Runnable apply, Runnable unapply) {
            super("texture", apply, unapply);
        }

        TextureBase() {
            super("texture", () -> {}, () -> {});
        }

        protected Optional<Identifier> getId() {
            return Optional.empty();
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Texturing
    extends RenderPhase {
        public Texturing(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Lightmap
    extends Toggleable {
        public Lightmap(boolean lightmap) {
            super("lightmap", () -> {
                if (lightmap) {
                    MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().enable();
                }
            }, () -> {
                if (lightmap) {
                    MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().disable();
                }
            }, lightmap);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Overlay
    extends Toggleable {
        public Overlay(boolean overlayColor) {
            super("overlay", () -> {
                if (overlayColor) {
                    MinecraftClient.getInstance().gameRenderer.getOverlayTexture().setupOverlayColor();
                }
            }, () -> {
                if (overlayColor) {
                    MinecraftClient.getInstance().gameRenderer.getOverlayTexture().teardownOverlayColor();
                }
            }, overlayColor);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Cull
    extends Toggleable {
        public Cull(boolean culling) {
            super("cull", () -> {
                if (!culling) {
                    RenderSystem.disableCull();
                }
            }, () -> {
                if (!culling) {
                    RenderSystem.enableCull();
                }
            }, culling);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class DepthTest
    extends RenderPhase {
        private final String depthFunctionName;

        public DepthTest(String depthFunctionName, int depthFunction) {
            super("depth_test", () -> {
                if (depthFunction != 519) {
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthFunc(depthFunction);
                }
            }, () -> {
                if (depthFunction != 519) {
                    RenderSystem.disableDepthTest();
                    RenderSystem.depthFunc(515);
                }
            });
            this.depthFunctionName = depthFunctionName;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.depthFunctionName + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class WriteMaskState
    extends RenderPhase {
        private final boolean color;
        private final boolean depth;

        public WriteMaskState(boolean color, boolean depth) {
            super("write_mask_state", () -> {
                if (!depth) {
                    RenderSystem.depthMask(depth);
                }
                if (!color) {
                    RenderSystem.colorMask(color, color, color, color);
                }
            }, () -> {
                if (!depth) {
                    RenderSystem.depthMask(true);
                }
                if (!color) {
                    RenderSystem.colorMask(true, true, true, true);
                }
            });
            this.color = color;
            this.depth = depth;
        }

        @Override
        public String toString() {
            return this.name + "[writeColor=" + this.color + ", writeDepth=" + this.depth + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Layering
    extends RenderPhase {
        public Layering(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Target
    extends RenderPhase {
        public Target(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class LineWidth
    extends RenderPhase {
        private final OptionalDouble width;

        public LineWidth(OptionalDouble width) {
            super("line_width", () -> {
                if (!Objects.equals(width, OptionalDouble.of(1.0))) {
                    if (width.isPresent()) {
                        RenderSystem.lineWidth((float)width.getAsDouble());
                    } else {
                        RenderSystem.lineWidth(Math.max(2.5f, (float)MinecraftClient.getInstance().getWindow().getFramebufferWidth() / 1920.0f * 2.5f));
                    }
                }
            }, () -> {
                if (!Objects.equals(width, OptionalDouble.of(1.0))) {
                    RenderSystem.lineWidth(1.0f);
                }
            });
            this.width = width;
        }

        @Override
        public String toString() {
            return this.name + "[" + String.valueOf(this.width.isPresent() ? Double.valueOf(this.width.getAsDouble()) : "window_scale") + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class ColorLogic
    extends RenderPhase {
        public ColorLogic(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Toggleable
    extends RenderPhase {
        private final boolean enabled;

        public Toggleable(String name, Runnable apply, Runnable unapply, boolean enabled) {
            super(name, apply, unapply);
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.enabled + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static final class OffsetTexturing
    extends Texturing {
        public OffsetTexturing(float x, float y) {
            super("offset_texturing", () -> RenderSystem.setTextureMatrix(new Matrix4f().translation(x, y, 0.0f)), () -> RenderSystem.resetTextureMatrix());
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Textures
    extends TextureBase {
        private final Optional<Identifier> id;

        Textures(ImmutableList<Triple<Identifier, Boolean, Boolean>> textures) {
            super(() -> {
                int i = 0;
                for (Triple triple : textures) {
                    TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                    textureManager.getTexture((Identifier)triple.getLeft()).setFilter((Boolean)triple.getMiddle(), (Boolean)triple.getRight());
                    RenderSystem.setShaderTexture(i++, (Identifier)triple.getLeft());
                }
            }, () -> {});
            this.id = textures.stream().findFirst().map(Triple::getLeft);
        }

        @Override
        protected Optional<Identifier> getId() {
            return this.id;
        }

        public static Builder create() {
            return new Builder();
        }

        @Environment(value=EnvType.CLIENT)
        public static final class Builder {
            private final ImmutableList.Builder<Triple<Identifier, Boolean, Boolean>> textures = new ImmutableList.Builder();

            public Builder add(Identifier id, boolean blur, boolean mipmap) {
                this.textures.add((Object)Triple.of(id, blur, mipmap));
                return this;
            }

            public Textures build() {
                return new Textures((ImmutableList<Triple<Identifier, Boolean, Boolean>>)this.textures.build());
            }
        }
    }
}

