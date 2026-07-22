package github.trollhack.modules.impl.hud;

import github.trollhack.events.impl.PacketEvent;
import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.utils.render.font.FontRenderers;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;

public class TPS extends HudModule {
    public static final TPS INSTANCE = new TPS();

    private long lastTimePacket = 0;
    private float currentTps = 20.0f;
    private final Deque<Float> tpsBuffer = new ArrayDeque<>();

    private static final int BUFFER_SIZE = 120;

    public TPS() {
        super("TPS", 2, 215, 40, 12);
        for (int i = 0; i < BUFFER_SIZE; i++) {
            tpsBuffer.addLast(20.0f);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            long now = System.currentTimeMillis();
            if (lastTimePacket != 0) {
                long diff = now - lastTimePacket;
                if (diff > 0) {
                    currentTps = Math.min(20.0f, 20000.0f / diff);
                }
            }
            lastTimePacket = now;
            tpsBuffer.addLast(currentTps);
            if (tpsBuffer.size() > BUFFER_SIZE) {
                tpsBuffer.pollFirst();
            }
        }
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;
        float sum = 0;
        for (float t : tpsBuffer) sum += t;
        String tpsText = String.format("%.2f", sum / tpsBuffer.size());
        MatrixStack matrices = context.getMatrices();
        Color textColor = GuiSetting.INSTANCE.getText();
        Color primaryColor = GuiSetting.INSTANCE.getPrimary();
        float x = getPosX() + 2f;
        float y = getPosY() + 2f;
        float s = 1.0f;
        FontRenderers.ducksans.drawText(matrices, tpsText, x, y, s, textColor);
        float w1 = FontRenderers.ducksans.getStringWidth(tpsText, s);
        FontRenderers.ducksans.drawText(matrices, " tps", x + w1, y, s, primaryColor);
        float totalW = w1 + FontRenderers.ducksans.getStringWidth(" tps", s);
        setWidth(totalW + 4f);
        setHeight(FontRenderers.ducksans.getStringHeight(s) + 4f);
    }
}
