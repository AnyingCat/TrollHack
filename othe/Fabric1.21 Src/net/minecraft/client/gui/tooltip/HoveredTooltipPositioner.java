/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@Environment(value=EnvType.CLIENT)
public class HoveredTooltipPositioner
implements TooltipPositioner {
    public static final TooltipPositioner INSTANCE = new HoveredTooltipPositioner();

    private HoveredTooltipPositioner() {
    }

    @Override
    public Vector2ic getPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {
        Vector2i vector2i = new Vector2i(x, y).add(12, -12);
        this.preventOverflow(screenWidth, screenHeight, vector2i, width, height);
        return vector2i;
    }

    private void preventOverflow(int screenWidth, int screenHeight, Vector2i pos, int width, int height) {
        int i;
        if (pos.x + width > screenWidth) {
            pos.x = Math.max(pos.x - 24 - width, 4);
        }
        if (pos.y + (i = height + 3) > screenHeight) {
            pos.y = screenHeight - i;
        }
    }
}

