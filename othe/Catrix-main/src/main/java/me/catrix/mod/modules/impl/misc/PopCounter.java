package me.catrix.mod.modules.impl.misc;

import me.catrix.Catrix;
import me.catrix.api.utils.math.MessageNotificationUtil;
import me.catrix.api.utils.render.RenderShadersUtil;
import me.catrix.mod.gui.font.FontRenderers;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.settings.impl.BooleanSetting;
import me.catrix.api.events.eventbus.EventHandler;
import me.catrix.api.events.impl.DeathEvent;
import me.catrix.api.events.impl.TotemEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PopCounter extends Module {

    public static PopCounter INSTANCE;
    public final BooleanSetting unPop = add(new BooleanSetting("Dead", true));

    public static final ArrayList<MessageNotificationUtil> notifyList = new ArrayList<>();

    public PopCounter() {
        super("PopCounter", "Counts players totem pops", Category.Misc);
        setChinese("图腾计数器");
        INSTANCE = this;
    }

    @EventHandler
    public void onPlayerDeath(DeathEvent event) {
        PlayerEntity player = event.getPlayer();
        if (Catrix.POP.popContainer.containsKey(player.getName().getString())) {
            int l_Count = Catrix.POP.popContainer.get(player.getName().getString());
            String message;
            if (l_Count == 1) {
                if (player.equals(mc.player)) {
                    message = "§fYou§r died after popping §f" + l_Count + "§r totem.";
                } else {
                    message = "§f" + player.getName().getString() + "§r died after popping §f" + l_Count + "§r totem.";
                }
            } else {
                if (player.equals(mc.player)) {
                    message = "§fYou§r died after popping §f" + l_Count + "§r totems.";
                } else {
                    message = "§f" + player.getName().getString() + "§r died after popping §f" + l_Count + "§r totems.";
                }
            }
            addNotification(message);
        } else if (unPop.getValue()) {
            String message;
            if (player.equals(mc.player)) {
                message = "§fYou§r died.";
            } else {
                message = "§f" + player.getName().getString() + "§r died.";
            }
            addNotification(message);
        }
    }

    @EventHandler
    public void onTotem(TotemEvent event) {
        PlayerEntity player = event.getPlayer();
        int l_Count = 1;
        if (Catrix.POP.popContainer.containsKey(player.getName().getString())) {
            l_Count = Catrix.POP.popContainer.get(player.getName().getString());
        }
        String message;
        if (l_Count == 1) {
            if (player.equals(mc.player)) {
                message = "§fYou§r popped §f" + l_Count + "§r totem.";
            } else {
                message = "§f" + player.getName().getString() + " §rpopped §f" + l_Count + "§r totem.";
            }
        } else {
            if (player.equals(mc.player)) {
                message = "§fYou§r popped §f" + l_Count + "§r totems.";
            } else {
                message = "§f" + player.getName().getString() + " §rhas popped §f" + l_Count + "§r totems.";
            }
        }
        addNotification(message);
    }

    private void addNotification(String message) {
        notifyList.add(new MessageNotificationUtil(message));
    }

    @Override
    public void onUpdate() {
        Iterator<MessageNotificationUtil> iterator = notifyList.iterator();
        while (iterator.hasNext()) {
            MessageNotificationUtil notification = iterator.next();
            if (notification == null || notification.message == null) {
                iterator.remove();
                continue;
            }
            notification.update();
            if (notification.shouldRemove()) {
                iterator.remove();
            }
        }
    }

    @Override
    public void onDisable() {
        notifyList.clear();
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (notifyList.isEmpty()) return;
        MatrixStack matrixStack = drawContext.getMatrices();
        List<MessageNotificationUtil> activeNotifications = notifyList.stream()
                .filter(MessageNotificationUtil::isActive)
                .toList();
        for (int i = 0; i < activeNotifications.size(); i++) {
            MessageNotificationUtil notification = activeNotifications.get(i);
            if (notification == null) continue;

            RenderShadersUtil.drawRoundedBlur(matrixStack,
                    (float) notification.getOffsetX((int) Math.max(FontRenderers.ui.getWidth(notification.message) + 26, 100), mc.getWindow().getScaledWidth()),
                    (float) notification.getOffsetY(20, mc.getWindow().getScaledHeight(), i),
                    (int) Math.max(FontRenderers.ui.getWidth(notification.message) + 26, 100), 20, 5.0f,  new Color(0x35000000, true), 15.0f, 0.55f);

            RenderShadersUtil.drawBlurredShadow(matrixStack,
                    (float) notification.getOffsetX((int) Math.max(FontRenderers.ui.getWidth(notification.message) + 26, 100), mc.getWindow().getScaledWidth()) - 2,
                    (float) notification.getOffsetY(20, mc.getWindow().getScaledHeight(), i) - 2,
                    (int) Math.max(FontRenderers.ui.getWidth(notification.message) + 26, 100), 20, 20, new Color(0x4C000000, true));

            FontRenderers.icon.drawString(matrixStack,
                    "I",
                    (float) notification.getOffsetX((int) Math.max(FontRenderers.ui.getWidth(notification.message) + 26, 100), mc.getWindow().getScaledWidth()) + 4,
                    (float) notification.getOffsetY(20, mc.getWindow().getScaledHeight(), i) + 4.8f,
                    new Color(255, 255, 0, (int) (255 * notification.getAlpha())).getRGB());

            FontRenderers.ui.drawString(matrixStack,
                    notification.message,
                    (float) notification.getOffsetX((int) Math.max(FontRenderers.ui.getWidth(notification.message) + 26, 100), mc.getWindow().getScaledWidth()) + 22.5,
                    (float) notification.getOffsetY(20, mc.getWindow().getScaledHeight(), i) + 6.8f,
                    new Color(255, 255, 255, (int) (255 * notification.getAlpha())).getRGB());
        }
    }
}