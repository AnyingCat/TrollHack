/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class RotatingCubeMapRenderer {
    public static final Identifier OVERLAY_TEXTURE = Identifier.ofVanilla("textures/gui/title/background/panorama_overlay.png");
    private final MinecraftClient client;
    private final CubeMapRenderer cubeMap;
    private float pitch;
    private float yaw;

    public RotatingCubeMapRenderer(CubeMapRenderer cubeMap) {
        this.cubeMap = cubeMap;
        this.client = MinecraftClient.getInstance();
    }

    public void render(DrawContext context, int width, int height, float alpha, float tickDelta) {
        float f = (float)((double)tickDelta * this.client.options.getPanoramaSpeed().getValue());
        this.pitch = RotatingCubeMapRenderer.wrapOnce(this.pitch + f * 0.1f, 360.0f);
        this.yaw = RotatingCubeMapRenderer.wrapOnce(this.yaw + f * 0.001f, (float)Math.PI * 2);
        this.cubeMap.draw(this.client, 10.0f, -this.pitch, alpha);
        RenderSystem.enableBlend();
        context.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        context.drawTexture(OVERLAY_TEXTURE, 0, 0, width, height, 0.0f, 0.0f, 16, 128, 16, 128);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private static float wrapOnce(float a, float b) {
        return a > b ? a - b : a;
    }
}

