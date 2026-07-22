package github.trollhack.modules.impl.hud;

import github.trollhack.modules.HudModule;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.awt.Color;

public class Armor extends HudModule {
    public static final Armor INSTANCE = new Armor();

    private final BooleanSetting classic = booleanSetting("Classic", false);
    private final BooleanSetting armorCount = booleanSetting("ArmorCount", true);
    private final BooleanSetting countElytras = booleanSetting("CountElytras", false, armorCount::getValue);
    private final BooleanSetting durabilityPercentage = booleanSetting("DurabilityPercentage", true);
    private final BooleanSetting durabilityBar = booleanSetting("DurabilityBar", false);

    private final int[] armorCounts = new int[4];

    public Armor() {
        super("Armor", 2, 220, 80, 80);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;
        if (mc.player == null) return;
        for (int i = 0; i < 4; i++) armorCounts[i] = 0;
        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            ItemStack stack = mc.player.getInventory().main.get(i);
            if (!stack.isEmpty()) addCount(stack);
        }
        for (ItemStack stack : mc.player.getInventory().armor) {
            if (!stack.isEmpty()) addCount(stack);
        }
        ItemStack offHand = mc.player.getInventory().offHand.get(0);
        if (!offHand.isEmpty()) addCount(offHand);

        MatrixStack matrices = context.getMatrices();
        float posX = getPosX();
        float posY = getPosY();
        float fontHeight = FontRenderers.ducksans.getStringHeight(1.0f);
        if (classic.getValue()) {
            float x = posX + 2f;
            float itemY = posY + fontHeight + 6f;
            float maxTextWidth = 0f;
            for (int i = 3; i >= 0; i--) {
                ItemStack stack = mc.player.getInventory().armor.get(i);
                if (!stack.isEmpty()) {
                    int itemXInt = (int) x;
                    int itemYInt = (int) itemY;
                    context.drawItem(stack, itemXInt, itemYInt);
                    float textW = drawDura(matrices, stack, itemXInt, itemYInt, x, posY + 2f, true);
                    maxTextWidth = Math.max(maxTextWidth, textW);
                    if (armorCount.getValue()) {
                        String countStr = String.valueOf(armorCounts[3 - i]);
                        float w = FontRenderers.ducksans.getStringWidth(countStr, 1.0f);
                        FontRenderers.ducksans.drawText(matrices, countStr, x + 16f - w, itemY + 16f - fontHeight, 1.0f, Color.WHITE);
                    }
                }
                x += 20f;
            }
            setWidth(Math.max(x - posX + 2f, 80f));
            setHeight(fontHeight + 22f);
        } else {
            float y = posY + 2f;
            float maxWidth = 0f;
            for (int i = 3; i >= 0; i--) {
                ItemStack stack = mc.player.getInventory().armor.get(i);
                if (!stack.isEmpty()) {
                    int itemXInt = (int) (posX + 2f);
                    int itemYInt = (int) y;
                    context.drawItem(stack, itemXInt, itemYInt);
                    float textX = posX + 22f;
                    float textY = y + 10f - fontHeight * 0.5f;
                    float textWidth = drawDura(matrices, stack, itemXInt, itemYInt, textX, textY, false);
                    if (armorCount.getValue()) {
                        String countStr = String.valueOf(armorCounts[3 - i]);
                        float w = FontRenderers.ducksans.getStringWidth(countStr, 1.0f);
                        FontRenderers.ducksans.drawText(matrices, countStr, posX + 2f + 16f - w, y + 16f - fontHeight, 1.0f, Color.WHITE);
                    }
                    maxWidth = Math.max(maxWidth, 22f + textWidth);
                }
                y += 20f;
            }
            setWidth(Math.max(maxWidth + 4f, 80f));
            setHeight(Math.max(y - posY, 80f));
        }
    }

    private void addCount(ItemStack stack) {
        if (stack.isOf(Items.DIAMOND_HELMET) || stack.isOf(Items.NETHERITE_HELMET)
                || stack.isOf(Items.IRON_HELMET) || stack.isOf(Items.GOLDEN_HELMET)
                || stack.isOf(Items.CHAINMAIL_HELMET) || stack.isOf(Items.LEATHER_HELMET)) {
            armorCounts[0]++;
        } else if ((stack.isOf(Items.DIAMOND_CHESTPLATE) || stack.isOf(Items.NETHERITE_CHESTPLATE)
                || stack.isOf(Items.IRON_CHESTPLATE) || stack.isOf(Items.GOLDEN_CHESTPLATE)
                || stack.isOf(Items.CHAINMAIL_CHESTPLATE) || stack.isOf(Items.LEATHER_CHESTPLATE))
                || (countElytras.getValue() && stack.isOf(Items.ELYTRA))) {
            armorCounts[1]++;
        } else if (stack.isOf(Items.DIAMOND_LEGGINGS) || stack.isOf(Items.NETHERITE_LEGGINGS)
                || stack.isOf(Items.IRON_LEGGINGS) || stack.isOf(Items.GOLDEN_LEGGINGS)
                || stack.isOf(Items.CHAINMAIL_LEGGINGS) || stack.isOf(Items.LEATHER_LEGGINGS)) {
            armorCounts[2]++;
        } else if (stack.isOf(Items.DIAMOND_BOOTS) || stack.isOf(Items.NETHERITE_BOOTS)
                || stack.isOf(Items.IRON_BOOTS) || stack.isOf(Items.GOLDEN_BOOTS)
                || stack.isOf(Items.CHAINMAIL_BOOTS) || stack.isOf(Items.LEATHER_BOOTS)) {
            armorCounts[3]++;
        }
    }

    private float drawDura(MatrixStack matrices, ItemStack stack, int itemX, int itemY,
                           float textX, float textY, boolean classic) {
        if (!stack.isDamageable()) return 0f;
        int maxDamage = stack.getMaxDamage();
        int dura = maxDamage - stack.getDamage();
        float duraMultiplier = maxDamage > 0 ? dura / (float) maxDamage : 0f;
        float duraPercent = Math.round(duraMultiplier * 1000f) / 10f;
        Color color;
        if (duraPercent <= 0f) {
            color = new Color(200, 20, 20, 255);
        } else if (duraPercent >= 100f) {
            color = new Color(20, 232, 20, 255);
        } else if (duraPercent <= 50f) {
            float t = duraPercent / 50f;
            color = new Color((int) (200 + (240 - 200) * t), (int) (20 + (220 - 20) * t), 20, 255);
        } else {
            float t = (duraPercent - 50f) / 50f;
            color = new Color((int) (240 + (20 - 240) * t), (int) (220 + (232 - 220) * t), 20, 255);
        }
        if (durabilityBar.getValue()) {
            Render2DUtil.drawRect(matrices, itemX, itemY + 16f, 16f, 2f, new Color(0, 0, 0, 255));
            Render2DUtil.drawRect(matrices, itemX, itemY + 16f, Math.max(0f, 16f * duraMultiplier), 2f, color);
        }
        if (durabilityPercentage.getValue()) {
            if (classic) {
                String str = String.valueOf((int) duraPercent);
                float w = FontRenderers.ducksans.getStringWidth(str, 1.0f);
                FontRenderers.ducksans.drawText(matrices, str, itemX + 8f - w * 0.5f, textY, 1.0f, color);
                return w;
            } else {
                String str = dura + "/" + maxDamage + "  (" + duraPercent + "%)";
                float w = FontRenderers.ducksans.getStringWidth(str, 1.0f);
                FontRenderers.ducksans.drawText(matrices, str, textX, textY, 1.0f, color);
                return w;
            }
        }
        return 0f;
    }
}
