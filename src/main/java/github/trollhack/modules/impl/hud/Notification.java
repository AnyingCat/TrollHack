package github.trollhack.modules.impl.hud;

import github.trollhack.events.impl.ModuleToggleEvent;
import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.modules.impl.misc.Friends;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.IntegerSetting;
import github.trollhack.utils.animation.AnimationUtil;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Notification extends HudModule {
    public static final Notification INSTANCE = new Notification();

    private final BooleanSetting moduleToggle = booleanSetting("ModuleToggle", true);
    private final IntegerSetting moduleToggleTimeout = integerSetting("ModuleToggleTimeout", 3000, 0, 10000, 100);
    private final IntegerSetting defaultTimeout = integerSetting("DefaultTimeout", 5000, 0, 10000, 100);
    private final BooleanSetting nvidia = booleanSetting("Nvidia", false);
    private final IntegerSetting backgroundAlpha = integerSetting("BackgroundAlpha", 180, 0, 255, 1, nvidia::getValue);

    private static final float MIN_WIDTH = 150.0f;
    private static final float SPACE = 4.0f;
    private static final float PADDING = 4.0f;

    private final List<NotificationMessage> notifications = new CopyOnWriteArrayList<>();
    private final Map<Long, NotificationMessage> messageMap = new ConcurrentHashMap<>();

    public Notification() {
        super("Notification", 2, 40, 150, 100);
    }

    @Override
    public boolean isRightAligned() {
        int screenWidth = mc.getWindow().getScaledWidth();
        return getPosX() + getWidth() / 2.0f > screenWidth / 2.0f;
    }

    @Override
    public float getRenderX() {
        return isRightAligned() ? getPosX() - getWidth() : getPosX();
    }

    @Override
    public float clampDragX(float newX) {
        if (isRightAligned()) {
            return Math.max(getWidth(), Math.min(mc.getWindow().getScaledWidth(), newX));
        }
        return super.clampDragX(newX);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onModuleToggle(ModuleToggleEvent event) {
        if (!moduleToggle.getValue()) return;
        if (event.getModule() instanceof Friends) return;
        String name = event.getModule().getName();
        String status = event.isEnabled() ? " Enabled" : " Disabled";
        Color statusColor = event.isEnabled() ? new Color(0, 255, 0) : new Color(255, 0, 0);
        long id = Notification.class.hashCode() * 31L + event.getModule().hashCode();
        synchronized (messageMap) {
            NotificationMessage existing = messageMap.get(id);
            if (existing != null && !existing.isTimeout()) {
                existing.update(name, status, statusColor, moduleToggleTimeout.getValue());
            } else {
                NotificationMessage msg = new NotificationMessage(name, status, statusColor, moduleToggleTimeout.getValue(), id);
                messageMap.put(id, msg);
                notifications.add(msg);
            }
        }
    }

    public static void send(String message) {
        send(message.hashCode(), message, INSTANCE.defaultTimeout.getValue());
    }

    public static void send(String message, long timeout) {
        send(message.hashCode(), message, timeout);
    }

    public static void send(long id, String message, long timeout) {
        synchronized (INSTANCE.messageMap) {
            NotificationMessage existing = INSTANCE.messageMap.get(id);
            if (existing != null && !existing.isTimeout()) {
                existing.update(message, timeout);
            } else {
                NotificationMessage msg = new NotificationMessage(message, timeout, id);
                INSTANCE.messageMap.put(id, msg);
                INSTANCE.notifications.add(msg);
            }
        }
    }

    public static void sendReplace(long id, String message, long timeout) {
        synchronized (INSTANCE.messageMap) {
            NotificationMessage existing = INSTANCE.messageMap.get(id);
            if (existing != null && !existing.isTimeout()) {
                existing.forceExit();
            }
            NotificationMessage msg = new NotificationMessage(message, timeout, id);
            INSTANCE.messageMap.put(id, msg);
            INSTANCE.notifications.add(msg);
        }
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;

        MatrixStack matrices = context.getMatrices();
        float fontHeight = FontRenderers.ducksans.getStringHeight(1.0f);
        float msgHeight = fontHeight * 4.0f;

        boolean rightAligned = isRightAligned();

        float currentY = getPosY();
        float maxWidth = MIN_WIDTH;

        for (NotificationMessage msg : notifications) {
            float renderResult = msg.render(matrices, getPosX(), currentY, rightAligned, fontHeight, msgHeight, this);
            if (renderResult < 0) {
                notifications.remove(msg);
                synchronized (messageMap) {
                    if (messageMap.get(msg.id) == msg) {
                        messageMap.remove(msg.id);
                    }
                }
            } else {
                currentY += renderResult;
                maxWidth = Math.max(maxWidth, msg.getWidth());
            }
        }

        setWidth(maxWidth);
        setHeight(Math.max(currentY - getPosY(), msgHeight));
    }

    private static class NotificationMessage {
        private String message;
        private String statusText;
        private Color statusColor;
        private long length;
        private final long id;
        private long startTime;

        NotificationMessage(String message, long length, long id) {
            this(message, null, null, length, id);
        }

        NotificationMessage(String message, String statusText, Color statusColor, long length, long id) {
            this.message = message;
            this.statusText = statusText;
            this.statusColor = statusColor;
            this.length = length;
            this.id = id;
            this.startTime = System.currentTimeMillis();
        }

        boolean isTimeout() {
            return System.currentTimeMillis() - startTime > length;
        }

        void forceExit() {
            this.startTime = System.currentTimeMillis() - this.length;
        }

        void update(String message, long length) {
            this.message = message;
            this.statusText = null;
            this.statusColor = null;
            this.length = length + (System.currentTimeMillis() - startTime);
        }

        void update(String message, String statusText, Color statusColor, long length) {
            this.message = message;
            this.statusText = statusText;
            this.statusColor = statusColor;
            this.length = length + (System.currentTimeMillis() - startTime);
        }

        float getWidth() {
            float textWidth = FontRenderers.ducksans.getStringWidth(message, 1.0f);
            if (statusText != null) {
                textWidth += FontRenderers.ducksans.getStringWidth(statusText, 1.0f);
            }
            return Math.max(MIN_WIDTH, PADDING + PADDING + textWidth + PADDING);
        }

        float render(MatrixStack matrices, float posX, float posY, boolean rightAligned,
                     float fontHeight, float msgHeight, Notification parent) {
            float width = getWidth();
            float renderX = rightAligned ? posX - width : posX;
            float stringPosX = rightAligned ? renderX + PADDING + PADDING : renderX + PADDING;
            float stringPosY = posY + msgHeight * 0.5f - 1.0f - fontHeight * 0.5f;

            Color accentColor;
            if (parent.nvidia.getValue()) {
                accentColor = new Color(118, 185, 0);
            } else {
                Color primary = GuiSetting.INSTANCE.getPrimary();
                accentColor = new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);
            }

            Color bgColor = parent.nvidia.getValue()
                ? new Color(0, 0, 0, parent.backgroundAlpha.getValue())
                : GuiSetting.INSTANCE.getBackGround();

            long deltaTotal = System.currentTimeMillis() - startTime;

            if (deltaTotal <= 299L) {
                float delta = deltaTotal / 300.0f;
                float progress = AnimationUtil.Easing.EASE_OUT_CUBIC.inc(delta);
                return renderStage1(matrices, renderX, posY, width, msgHeight, progress, rightAligned, accentColor);
            } else if (deltaTotal <= 500L) {
                float delta = (deltaTotal - 300L) / 200.0f;
                float progress = AnimationUtil.Easing.EASE_OUT_CUBIC.inc(delta);
                return renderStage2(matrices, renderX, posY, width, msgHeight, progress, rightAligned,
                        accentColor, bgColor, stringPosX, stringPosY);
            } else if (deltaTotal < length) {
                Render2DUtil.drawRect(matrices, renderX, posY, width, msgHeight, bgColor);
                if (rightAligned) {
                    Render2DUtil.drawRect(matrices, renderX, posY, PADDING, msgHeight, accentColor);
                } else {
                    Render2DUtil.drawRect(matrices, renderX + width - PADDING, posY, PADDING, msgHeight, accentColor);
                }
                drawText(matrices, stringPosX, stringPosY, 1.0f);
                return msgHeight + SPACE;
            } else {
                long endDelta = deltaTotal - length;
                if (endDelta <= 199L) {
                    float delta = endDelta / 200.0f;
                    float progress = AnimationUtil.Easing.EASE_OUT_CUBIC.dec(delta);
                    return renderStage2(matrices, renderX, posY, width, msgHeight, progress, rightAligned,
                            accentColor, bgColor, stringPosX, stringPosY);
                } else if (endDelta <= 500L) {
                    float delta = (endDelta - 200L) / 300.0f;
                    float progress = AnimationUtil.Easing.EASE_OUT_CUBIC.dec(delta);
                    return renderStage1(matrices, renderX, posY, width, msgHeight, progress, rightAligned, accentColor);
                } else {
                    return -1.0f;
                }
            }
        }

        private void drawText(MatrixStack matrices, float x, float y, float alpha) {
            Color nameColor = new Color(255, 255, 255, (int) (255.0f * alpha));
            FontRenderers.ducksans.drawText(matrices, message, x, y, 1.0f, nameColor);
            if (statusText != null) {
                float nameWidth = FontRenderers.ducksans.getStringWidth(message, 1.0f);
                Color sc = new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), (int) (255.0f * alpha));
                FontRenderers.ducksans.drawText(matrices, statusText, x + nameWidth, y, 1.0f, sc);
            }
        }

        private float renderStage1(MatrixStack matrices, float x, float y, float width, float height,
                                    float progress, boolean rightAligned, Color color) {
            if (rightAligned) {
                float barX = x + MIN_WIDTH * (1.0f - progress);
                float barW = (x + width) - barX;
                if (barW > 0) Render2DUtil.drawRect(matrices, barX, y, barW, height, color);
            } else {
                float barW = width * progress;
                if (barW > 0) Render2DUtil.drawRect(matrices, x, y, barW, height, color);
            }
            return (height + SPACE) * progress;
        }

        private float renderStage2(MatrixStack matrices, float x, float y, float width, float height,
                                    float progress, boolean rightAligned, Color accentColor, Color bgColor,
                                    float stringPosX, float stringPosY) {
            Render2DUtil.drawRect(matrices, x, y, width, height, bgColor);

            if (progress > 0.01f) {
                drawText(matrices, stringPosX, stringPosY, progress);
            }

            if (rightAligned) {
                float barW = PADDING + (width - PADDING) * (1.0f - progress);
                if (barW > 0) Render2DUtil.drawRect(matrices, x, y, barW, height, accentColor);
            } else {
                float barX = x + (width - PADDING) * progress;
                float barW = (x + width) - barX;
                if (barW > 0) Render2DUtil.drawRect(matrices, barX, y, barW, height, accentColor);
            }

            return height + SPACE;
        }
    }
}
