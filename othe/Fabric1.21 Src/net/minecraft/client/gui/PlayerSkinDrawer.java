/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

/**
 * Helper class for drawing a player's head on GUI.
 */
@Environment(value=EnvType.CLIENT)
public class PlayerSkinDrawer {
    public static final int FACE_WIDTH = 8;
    public static final int FACE_HEIGHT = 8;
    public static final int FACE_X = 8;
    public static final int FACE_Y = 8;
    public static final int FACE_OVERLAY_X = 40;
    public static final int FACE_OVERLAY_Y = 8;
    public static final int field_39531 = 8;
    public static final int field_39532 = 8;
    public static final int SKIN_TEXTURE_WIDTH = 64;
    public static final int SKIN_TEXTURE_HEIGHT = 64;

    public static void draw(DrawContext context, SkinTextures textures, int x, int y, int size) {
        PlayerSkinDrawer.draw(context, textures.texture(), x, y, size);
    }

    /**
     * Draws the player's head (including the hat) on GUI.
     */
    public static void draw(DrawContext context, Identifier texture, int x, int y, int size) {
        PlayerSkinDrawer.draw(context, texture, x, y, size, true, false);
    }

    /**
     * Draws the player's head on GUI.
     */
    public static void draw(DrawContext context, Identifier texture, int x, int y, int size, boolean hatVisible, boolean upsideDown) {
        int i = 8 + (upsideDown ? 8 : 0);
        int j = 8 * (upsideDown ? -1 : 1);
        context.drawTexture(texture, x, y, size, size, 8.0f, i, 8, j, 64, 64);
        if (hatVisible) {
            PlayerSkinDrawer.drawHat(context, texture, x, y, size, upsideDown);
        }
    }

    private static void drawHat(DrawContext context, Identifier texture, int x, int y, int size, boolean upsideDown) {
        int i = 8 + (upsideDown ? 8 : 0);
        int j = 8 * (upsideDown ? -1 : 1);
        RenderSystem.enableBlend();
        context.drawTexture(texture, x, y, size, size, 40.0f, i, 8, j, 64, 64);
        RenderSystem.disableBlend();
    }
}

