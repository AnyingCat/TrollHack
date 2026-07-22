package me.catrix.mod.modules.impl.client;

import me.catrix.Catrix;
import me.catrix.api.utils.render.RenderShadersUtil;
import me.catrix.api.utils.render.TextUtil;
import me.catrix.mod.gui.font.FontRenderers;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.settings.impl.BooleanSetting;
import me.catrix.mod.modules.settings.impl.SliderSetting;
import me.catrix.mod.modules.settings.impl.StringSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.world.World;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HUD extends Module {
    public static HUD INSTANCE;
    public final BooleanSetting up = add(new BooleanSetting("Up", false));
    public final BooleanSetting customFont = add(new BooleanSetting("CustomFont", true));

    public final BooleanSetting waterMark = add(new BooleanSetting("WaterMark", true).setParent());
    public final StringSetting waterMarkString = add(new StringSetting("Title", Catrix.NAME + " " + Catrix.VERSION, waterMark::isOpen));
    public final SliderSetting offset = add(new SliderSetting("Offset", 4, 0, 100, -1, waterMark::isOpen));
    public final BooleanSetting lowerCase = add(new BooleanSetting("LowerCase", false));
    public final BooleanSetting ping = add(new BooleanSetting("Ping", true));
    public final BooleanSetting tps = add(new BooleanSetting("TPS", true));
    public final BooleanSetting speed = add(new BooleanSetting("Speed", true));
    public final BooleanSetting brand = add(new BooleanSetting("Brand", false));
    public final BooleanSetting coords = add(new BooleanSetting("Coords", true));
    public HUD() {
        super("HUD", Category.Client);
        setChinese("界面");
        INSTANCE = this;
    }

    private final DecimalFormat decimal = new DecimalFormat("0.0");

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (waterMark.getValue()) {
            RenderShadersUtil.drawBlurredShadow(drawContext.getMatrices(), offset.getValueInt() - 4, offset.getValueInt() - 4,
                    getWidth(waterMarkString.getValue() + " | " + (mc.player != null ? mc.player.getGameProfile().getName() : "Unknown") + " | " + (new SimpleDateFormat("h:mm a", Locale.ENGLISH)).format(new Date()) + " | " + (mc.isInSingleplayer() ? "SinglePlayer" : mc.getCurrentServerEntry().address)) + 8, getHeight() + 8, 15, new Color(0x66000000,true));
            RenderShadersUtil.drawRect(drawContext.getMatrices(), offset.getValueInt() - 2, offset.getValueInt() - 2,
                    getWidth(waterMarkString.getValue() + " | " + (mc.player != null ? mc.player.getGameProfile().getName() : "Unknown") + " | " + (new SimpleDateFormat("h:mm a", Locale.ENGLISH)).format(new Date()) + " | " + (mc.isInSingleplayer() ? "SinglePlayer" : mc.getCurrentServerEntry().address)) + 4, getHeight() + 4, 3f, new Color(0x7C000000,true));
            drawText(drawContext, waterMarkString.getValue(), offset.getValueInt(), offset.getValueInt());
            boolean useCustomFont = customFont.getValue() && !containsChinese(mc.player.getGameProfile().getName());
            TextUtil.drawString(drawContext, " | " + (mc.player != null ? mc.player.getGameProfile().getName() : "Unknown") + " | " + (new SimpleDateFormat("h:mm a", Locale.ENGLISH)).format(new Date()) + " | " + (mc.isInSingleplayer() ? "SinglePlayer" : mc.getCurrentServerEntry().address),
                    offset.getValueInt() + getWidth(waterMarkString.getValue()), offset.getValueInt(), Color.WHITE.getRGB(), useCustomFont);
        }
        int fontHeight = getHeight();
        int height;
        int y;
        if (up.getValue()) {
            y = 1;
            height = -fontHeight;
        } else {
            y = mc.getWindow().getScaledHeight() - fontHeight;
            if (mc.currentScreen instanceof ChatScreen) {
                y -= 15;
            }
            height = fontHeight;
        }
        int windowWidth = mc.getWindow().getScaledWidth() - 1;
        if (brand.getValue()) {
            String brand = (mc.isInSingleplayer() ? "Vanilla" : mc.getNetworkHandler().getBrand().replaceAll("\\(.*?\\)", ""));
            int x = getWidth("ServerBrand " + brand);
            drawText(drawContext, "ServerBrand §f" + brand, windowWidth - x, y);
            y -= height;
        }
        if (tps.getValue()) {
            int x = getWidth("TPS " + Catrix.SERVER.getTPS() + " [" + Catrix.SERVER.getCurrentTPS() + "]");
            drawText(drawContext, "TPS §f" + Catrix.SERVER.getTPS() + " §7[§f" + Catrix.SERVER.getCurrentTPS() + "§7]", windowWidth - x, y);
            y -= height;
        }
        if (speed.getValue()) {
            double x = mc.player.getX() - mc.player.prevX;
            // double y = mc.player.getY() - mc.player.prevY;
            double z = mc.player.getZ() - mc.player.prevZ;
            double dist = Math.sqrt(x * x + z * z) / 1000.0;
            double div = 0.05 / 3600.0;
            float timer = Catrix.TIMER.get();
            final double speed = dist / div * timer;
            String text = String.format("Speed §f%skm/h",
                    decimal.format(speed));
            int width = getWidth(text);
            drawText(drawContext, text, windowWidth - width, y);
            y -= height;
        }
        if (ping.getValue()) {
            PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            String ping;
            if (playerListEntry == null) {
                ping = "Unknown";
            } else {
                ping = String.valueOf(playerListEntry.getLatency());
            }
            int x = getWidth("Ping " + ping);
            drawText(drawContext, "Ping §f" + ping, windowWidth - x, y);
            y -= height;
        }

        if (coords.getValue()) {
            boolean inNether = mc.world.getRegistryKey().equals(World.NETHER);

            int posX = mc.player.getBlockX();
            int posY = mc.player.getBlockY();
            int posZ = mc.player.getBlockZ();

            float factor = !inNether ? 0.125F : 8.0F;

            int anotherWorldX = (int) (mc.player.getX() * factor);
            int anotherWorldZ = (int) (mc.player.getZ() * factor);

            String coordsString = "XYZ §f" + (inNether ? (posX + ", " + posY + ", " + posZ + " §7[§f" + anotherWorldX + ", " + anotherWorldZ + "§7]§f") : (posX + ", " + posY + ", " + posZ + "§7 [§f" + anotherWorldX + ", " + anotherWorldZ + "§7]"));

            drawText(drawContext, coordsString, (int) 2.0F, mc.getWindow().getScaledHeight() - fontHeight - (mc.currentScreen instanceof ChatScreen ? 15 : 0));
        }
    }

    private int getWidth(String s) {
        if (customFont.getValue()) {
            return (int) FontRenderers.ui.getWidth(s);
        }
        return mc.textRenderer.getWidth(s);
    }

    private int getHeight() {
        if (customFont.getValue()) {
            return (int) FontRenderers.ui.getFontHeight();
        }
        return mc.textRenderer.fontHeight;
    }

    private void drawText(DrawContext drawContext, String s, int x, int y) {
        boolean useCustomFont = customFont.getValue() && !containsChinese(s);
        ModuleList.INSTANCE.counter--;
        if (lowerCase.getValue()) {
            s = s.toLowerCase();
        }
        TextUtil.drawString(drawContext, s, x, y, Colors.INSTANCE.getColor(ModuleList.INSTANCE.counter), useCustomFont);
    }

    private boolean containsChinese(String str) {
        for (char c : str.toCharArray()) {
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }
}