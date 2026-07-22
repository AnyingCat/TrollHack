package github.trollhack.modules.impl.hud;

import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Time extends HudModule {
    public static final Time INSTANCE = new Time();

    public enum DateFormat {
        DDMMYY("dd/MM/yy"),
        MMDDYY("MM/dd/yy"),
        YYMMDD("yy/MM/dd");

        public final String pattern;
        DateFormat(String pattern) { this.pattern = pattern; }
    }

    public enum TimeFormat {
        HHMM("HH:mm"),
        HHMMSS("HH:mm:ss");

        public final String pattern;
        TimeFormat(String pattern) { this.pattern = pattern; }
    }

    public enum TimeUnit {
        H24("24H"),
        H12("12H");

        public final String label;
        TimeUnit(String label) { this.label = label; }
    }

    private final BooleanSetting showDate = booleanSetting("ShowDate", true);
    private final BooleanSetting showTime = booleanSetting("ShowTime", true);
    private final EnumSetting<DateFormat> dateFormat = enumSetting("DateFormat", DateFormat.DDMMYY);
    private final EnumSetting<TimeFormat> timeFormat = enumSetting("TimeFormat", TimeFormat.HHMM);
    private final EnumSetting<TimeUnit> timeUnit = enumSetting("TimeUnit", TimeUnit.H24);

    public Time() {
        super("Time", 2, 230, 60, 24);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;
        Date now = new Date();
        Color textColor = GuiSetting.INSTANCE.getText();
        Color primaryColor = GuiSetting.INSTANCE.getPrimary();
        MatrixStack matrices = context.getMatrices();
        float x = getPosX() + 2f;
        float y = getPosY() + 2f;
        float s = 1.0f;
        float totalW = 0f;
        float totalH = FontRenderers.ducksans.getStringHeight(s) + 2f;
        if (showDate.getValue()) {
            String dateStr = new SimpleDateFormat(dateFormat.getValue().pattern).format(now);
            FontRenderers.ducksans.drawText(matrices, dateStr, x, y, s, primaryColor);
            totalW = Math.max(totalW, FontRenderers.ducksans.getStringWidth(dateStr, s));
            y += totalH;
        }
        if (showTime.getValue()) {
            String pattern = timeFormat.getValue().pattern;
            if (timeUnit.getValue() == TimeUnit.H12) {
                pattern = pattern.replace("HH", "hh") + " a";
            }
            String timeStr = new SimpleDateFormat(pattern).format(now);
            FontRenderers.ducksans.drawText(matrices, timeStr, x, y, s, primaryColor);
            totalW = Math.max(totalW, FontRenderers.ducksans.getStringWidth(timeStr, s));
            y += totalH;
        }
        setWidth(totalW + 4f);
        setHeight(y - getPosY() + 2f);
    }
}
