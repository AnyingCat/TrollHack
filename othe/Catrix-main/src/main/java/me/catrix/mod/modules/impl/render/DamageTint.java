package me.catrix.mod.modules.impl.render;

import me.catrix.api.utils.math.MathUtil;
import me.catrix.api.utils.render.ColorUtil;
import me.catrix.api.utils.render.Render2DUtil;
import me.catrix.mod.modules.Module;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class DamageTint extends Module {
    public DamageTint() {
        super("DamageTint", Category.Render);
        setChinese("受伤预警");
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        float factor = 1f - MathUtil.clamp(mc.player.getHealth(), 0f, 12f) / 12f;
        Color red = new Color(0xFF0000, true);
        if (factor < 1f)
            Render2DUtil.draw2DGradientRect(context.getMatrices(), 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), ColorUtil.injectAlpha(red, (int) (factor * 170f)), red, ColorUtil.injectAlpha(red, (int) (factor * 170f)), red);
    }
}
