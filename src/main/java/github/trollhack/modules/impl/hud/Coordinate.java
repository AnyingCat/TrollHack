package github.trollhack.modules.impl.hud;

import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.IntegerSetting;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.awt.Color;
import java.util.Locale;

public class Coordinate extends HudModule {
    public static final Coordinate INSTANCE = new Coordinate();

    private final BooleanSetting showX = booleanSetting("ShowX", true);
    private final BooleanSetting showY = booleanSetting("ShowY", true);
    private final BooleanSetting showZ = booleanSetting("ShowZ", true);
    private final BooleanSetting showNetherOverworld = booleanSetting("ShowNetherOverworld", true);
    private final IntegerSetting decimalPlaces = integerSetting("DecimalPlaces", 1, 0, 4, 1);
    private final BooleanSetting thousandsSeparator = booleanSetting("ThousandsSeparator", false);

    public Coordinate() {
        super("Coordinate", 2, 265, 80, 50);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;
        if (mc.player == null || mc.world == null) return;
        Vec3d pos = mc.player.getPos();
        RegistryKey<World> dimKey = mc.world.getRegistryKey();
        Vec3d otherDimPos = null;
        if (showNetherOverworld.getValue()) {
            if (dimKey == World.NETHER) {
                otherDimPos = new Vec3d(pos.x * 8.0, pos.y, pos.z * 8.0);
            } else if (dimKey == World.OVERWORLD) {
                otherDimPos = new Vec3d(pos.x * 0.125, pos.y, pos.z * 0.125);
            }
        }
        Color textColor = GuiSetting.INSTANCE.getText();
        Color primaryColor = GuiSetting.INSTANCE.getPrimary();
        MatrixStack matrices = context.getMatrices();
        float x = getPosX() + 2f;
        float y = getPosY() + 2f;
        float s = 1.0f;
        float cursorX = x;
        float fontHeight = FontRenderers.ducksans.getStringHeight(s);
        if (showX.getValue()) {
            cursorX += drawAxisSegment(matrices, "X", pos.x, otherDimPos != null ? otherDimPos.x : null, cursorX, y, s, primaryColor, textColor, cursorX != x);
        }
        if (showY.getValue()) {
            cursorX += drawAxisSegment(matrices, "Y", pos.y, otherDimPos != null ? otherDimPos.y : null, cursorX, y, s, primaryColor, textColor, cursorX != x);
        }
        if (showZ.getValue()) {
            cursorX += drawAxisSegment(matrices, "Z", pos.z, otherDimPos != null ? otherDimPos.z : null, cursorX, y, s, primaryColor, textColor, cursorX != x);
        }
        setWidth(cursorX - getPosX() + 2f);
        setHeight(fontHeight + 4f);
    }

    private float drawAxisSegment(MatrixStack matrices, String axis, double value, Double otherValue,
                                  float x, float y, float s, Color primaryColor, Color textColor, boolean prependSeparator) {
        float totalW = 0f;
        if (prependSeparator) {
            String sep = "  ";
            FontRenderers.ducksans.drawText(matrices, sep, x, y, s, primaryColor);
            totalW += FontRenderers.ducksans.getStringWidth(sep, s);
        }
        String label = axis + ": ";
        FontRenderers.ducksans.drawText(matrices, label, x + totalW, y, s, primaryColor);
        totalW += FontRenderers.ducksans.getStringWidth(label, s);
        String valueStr = roundOrInt(value);
        FontRenderers.ducksans.drawText(matrices, valueStr, x + totalW, y, s, textColor);
        totalW += FontRenderers.ducksans.getStringWidth(valueStr, s);
        if (otherValue != null) {
            String bracketStr = " [" + roundOrInt(otherValue) + "]";
            FontRenderers.ducksans.drawText(matrices, bracketStr, x + totalW, y, s, textColor);
            totalW += FontRenderers.ducksans.getStringWidth(bracketStr, s);
        }
        return totalW;
    }

    private String roundOrInt(double input) {
        String format = "%" + (thousandsSeparator.getValue() ? "," : "") + "." + decimalPlaces.getValue() + "f";
        return String.format(Locale.US, format, input);
    }
}
