package github.trollhack.modules.impl.hud;

import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;

public class CPS extends HudModule {
    public static final CPS INSTANCE = new CPS();

    private final FloatSetting averageSpeedTime = floatSetting("AverageSpeedTime", 2.0f, 1.0f, 5.0f, 0.1f);

    private final Deque<Long> clicks = new ArrayDeque<>();
    private boolean wasAttackDown = false;

    public CPS() {
        super("CPS", 2, 140, 40, 12);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;
        boolean isAttackDown = mc.options.attackKey.isPressed();
        if (isAttackDown && !wasAttackDown) {
            clicks.addLast(System.currentTimeMillis());
        }
        wasAttackDown = isAttackDown;
        long removeTime = System.currentTimeMillis() - (long) (averageSpeedTime.getValue() * 1000.0f);
        while (!clicks.isEmpty() && clicks.peekFirst() < removeTime) {
            clicks.pollFirst();
        }
        String cpsText = String.format("%.2f", clicks.size() / averageSpeedTime.getValue());
        MatrixStack matrices = context.getMatrices();
        Color textColor = GuiSetting.INSTANCE.getText();
        Color primaryColor = GuiSetting.INSTANCE.getPrimary();
        float x = getPosX() + 2f;
        float y = getPosY() + 2f;
        float s = 1.0f;
        FontRenderers.ducksans.drawText(matrices, cpsText, x, y, s, textColor);
        float w1 = FontRenderers.ducksans.getStringWidth(cpsText, s);
        FontRenderers.ducksans.drawText(matrices, " CPS", x + w1, y, s, primaryColor);
        float totalW = w1 + FontRenderers.ducksans.getStringWidth(" CPS", s);
        setWidth(totalW + 4f);
        setHeight(FontRenderers.ducksans.getStringHeight(s) + 4f);
    }
}
