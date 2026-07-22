package github.trollhack.modules.impl.render;

import github.trollhack.core.Managers;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.ColorSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.IntegerSetting;
import github.trollhack.utils.render.ProjectionUtil;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ESP2D extends Module {
    public static final ESP2D INSTANCE = new ESP2D();

    public final EnumSetting<Page> page = enumSetting("Page", Page.ENTITY_TYPE);

    public final BooleanSetting outline = booleanSetting("Outline", true, () -> page.getValue() == Page.OUTLINE);
    public final EnumSetting<BoxMode> boxMode = enumSetting("BoxMode", BoxMode.Corners, () -> page.getValue() == Page.OUTLINE && outline.getValue());
    public final EnumSetting<ColorMode> colorMode = enumSetting("ColorMode", ColorMode.Custom, () -> page.getValue() == Page.OUTLINE && outline.getValue());
    public final ColorSetting customColor = colorSetting("Color", new Color(200, 200, 200), () -> page.getValue() == Page.OUTLINE && outline.getValue() && colorMode.getValue() == ColorMode.Custom);
    public final FloatSetting saturation = floatSetting("Saturation", 1.0f, 0.0f, 1.0f, 0.1f, () -> page.getValue() == Page.OUTLINE && outline.getValue() && colorMode.getValue() != ColorMode.Custom);
    public final FloatSetting brightness = floatSetting("Brightness", 1.0f, 0.0f, 1.0f, 0.1f, () -> page.getValue() == Page.OUTLINE && outline.getValue() && colorMode.getValue() != ColorMode.Custom);
    public final IntegerSetting mixerSeconds = integerSetting("Seconds", 2, 1, 10, 1, () -> page.getValue() == Page.OUTLINE && outline.getValue() && colorMode.getValue() != ColorMode.Custom);

    public final BooleanSetting healthBar = booleanSetting("HealthBar", true, () -> page.getValue() == Page.HEALTH);
    public final EnumSetting<HpBarMode> hpBarMode = enumSetting("HBarMode", HpBarMode.Dot, () -> page.getValue() == Page.HEALTH && healthBar.getValue());
    public final BooleanSetting healthNumber = booleanSetting("HealthNumber", true, () -> page.getValue() == Page.HEALTH && healthBar.getValue());
    public final EnumSetting<HpMode> hpMode = enumSetting("HpMode", HpMode.Health, () -> page.getValue() == Page.HEALTH && healthBar.getValue() && healthNumber.getValue());
    public final BooleanSetting bbtt = booleanSetting("2B2TMode", true, () -> page.getValue() == Page.HEALTH && healthBar.getValue());

    public final BooleanSetting tags = booleanSetting("Tags", true, () -> page.getValue() == Page.TAGS);
    public final BooleanSetting itemTags = booleanSetting("ItemTags", true, () -> page.getValue() == Page.TAGS);
    public final BooleanSetting itemRender = booleanSetting("Item", true, () -> page.getValue() == Page.TAGS && itemTags.getValue());
    public final BooleanSetting tagsBG = booleanSetting("TagsBG", true, () -> page.getValue() == Page.TAGS);

    public final BooleanSetting armorBar = booleanSetting("ArmorBar", true, () -> page.getValue() == Page.ARMOR);
    public final BooleanSetting armorItems = booleanSetting("ArmorItems", true, () -> page.getValue() == Page.ARMOR);
    public final BooleanSetting armorDur = booleanSetting("ArmorDur", true, () -> page.getValue() == Page.ARMOR && armorItems.getValue());

    public final ColorSetting friendColor = colorSetting("FriendColor", new Color(0, 255, 255), () -> page.getValue() == Page.ENTITY_TYPE);
    public final FloatSetting range = floatSetting("Range", 32.0f, 8.0f, 64.0f, 0.5f, () -> page.getValue() == Page.ENTITY_TYPE);
    public final BooleanSetting self = booleanSetting("Self", true, () -> page.getValue() == Page.ENTITY_TYPE);
    public final BooleanSetting players = booleanSetting("Players", true, () -> page.getValue() == Page.ENTITY_TYPE);
    public final BooleanSetting mobs = booleanSetting("Mobs", false, () -> page.getValue() == Page.ENTITY_TYPE);
    public final BooleanSetting animals = booleanSetting("Animals", false, () -> page.getValue() == Page.ENTITY_TYPE);
    public final BooleanSetting droppedItems = booleanSetting("DroppedItems", false, () -> page.getValue() == Page.ENTITY_TYPE);
    public final BooleanSetting crystals = booleanSetting("Crystals", false, () -> page.getValue() == Page.ENTITY_TYPE);

    private final FloatSetting fontScale = floatSetting("FontScale", 0.75f, 0.0f, 1.0f, 0.05f, () -> page.getValue() == Page.TAGS);

    private final DecimalFormat dFormat = new DecimalFormat("0.0");

    public enum Page {
        ENTITY_TYPE, OUTLINE, HEALTH, TAGS, ARMOR
    }

    public enum BoxMode {
        Box, Corners
    }

    public enum HpBarMode {
        Dot, Line
    }

    public enum HpMode {
        Health, Percent
    }

    public enum ColorMode {
        Custom, Slowly, AnotherRainbow
    }

    public ESP2D() {
        super("ESP2D", Category.RENDER);
    }

    @Override
    public void onRender2D(DrawContext context) {
        if (nullCheck()) return;

        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        List<Entity> collected = collectEntities();

        for (Entity entity : collected) {
            Color entityColor = getColor(entity);

            double x = MathHelper.lerp(tickDelta, entity.prevX, entity.getX());
            double y = MathHelper.lerp(tickDelta, entity.prevY, entity.getY());
            double z = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ());

            double w = entity.getBoundingBox().getLengthX() / 1.5;
            double h = entity.getBoundingBox().getLengthY() + (entity.isSneaking() ? -0.3 : 0.2);

            Vec3d[] corners = new Vec3d[]{
                    new Vec3d(x - w, y, z - w),
                    new Vec3d(x - w, y + h, z - w),
                    new Vec3d(x + w, y, z - w),
                    new Vec3d(x + w, y + h, z - w),
                    new Vec3d(x - w, y, z + w),
                    new Vec3d(x - w, y + h, z + w),
                    new Vec3d(x + w, y, z + w),
                    new Vec3d(x + w, y + h, z + w)
            };

            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
            boolean valid = false;

            for (Vec3d corner : corners) {
                Vec3d screen = ProjectionUtil.worldToScreen(corner);
                if (screen == null || screen.z <= 0 || screen.z >= 1) continue;
                valid = true;
                minX = Math.min(screen.x, minX);
                minY = Math.min(screen.y, minY);
                maxX = Math.max(screen.x, maxX);
                maxY = Math.max(screen.y, maxY);
            }

            if (!valid) continue;

            double posX = minX;
            double posY = minY;
            double endPosX = maxX;
            double endPosY = maxY;

            if (outline.getValue()) {
                drawOutline(context.getMatrices(), posX, posY, endPosX, endPosY, entityColor.getRGB());
            }

            if (entity instanceof LivingEntity living) {
                if (healthBar.getValue()) {
                    drawHealthBar(context, living, posX, posY, endPosX, endPosY);
                }

                if (tags.getValue() && living instanceof PlayerEntity player) {
                    drawNameTag(context, player, posX, posY, endPosX, endPosY);
                }

                if (living instanceof PlayerEntity player) {
                    if (armorBar.getValue()) {
                        drawArmorBar(context.getMatrices(), player, posX, posY, endPosX, endPosY);
                    }

                    if (armorItems.getValue()) {
                        drawArmorItems(context, player, posX, posY, endPosX, endPosY);
                    }
                }

                if (itemTags.getValue()) {
                    drawItemTags(context, living, posX, posY, endPosX, endPosY);
                }
            }
        }
    }

    private void drawOutline(net.minecraft.client.util.math.MatrixStack matrices, double posX, double posY, double endPosX, double endPosY, int color) {
        int black = Color.BLACK.getRGB();

        if (boxMode.getValue() == BoxMode.Box) {
            Render2DUtil.drawRect(matrices, (float) (posX - 1), (float) posY, 1.5f, (float) (endPosY - posY + 0.5), new Color(black, true));
            Render2DUtil.drawRect(matrices, (float) (posX - 1), (float) (posY - 0.5), (float) (endPosX - posX + 1.5), 1f, new Color(black, true));
            Render2DUtil.drawRect(matrices, (float) (endPosX - 0.5), (float) posY, 1f, (float) (endPosY - posY + 0.5), new Color(black, true));
            Render2DUtil.drawRect(matrices, (float) (posX - 1), (float) (endPosY - 0.5), (float) (endPosX - posX + 1.5), 1f, new Color(black, true));

            Render2DUtil.drawRect(matrices, (float) (posX - 0.5), (float) posY, 0.5f, (float) (endPosY - posY), new Color(color, true));
            Render2DUtil.drawRect(matrices, (float) posX, (float) (endPosY - 0.5), (float) (endPosX - posX), 0.5f, new Color(color, true));
            Render2DUtil.drawRect(matrices, (float) (posX - 0.5), (float) posY, (float) (endPosX - posX + 0.5), 0.5f, new Color(color, true));
            Render2DUtil.drawRect(matrices, (float) (endPosX - 0.5), (float) posY, 0.5f, (float) (endPosY - posY), new Color(color, true));
        } else {
            double h4 = (endPosY - posY) / 4.0;
            double w3 = (endPosX - posX) / 3.0;

            Render2DUtil.drawRect(matrices, (float) (posX - 1), (float) (posY - 0.5), 1.5f, (float) (h4 + 0.5), new Color(black, true));
            Render2DUtil.drawRect(matrices, (float) (posX - 1), (float) (endPosY - h4 - 0.5), 1.5f, (float) (h4 + 0.5), new Color(black, true));
            Render2DUtil.drawRect(matrices, (float) (posX - 1), (float) (posY - 0.5), (float) (w3 + 1.5), 1f, new Color(black, true));
            Render2DUtil.drawRect(matrices, (float) (endPosX - w3 - 0.5), (float) (posY - 0.5), (float) (w3 + 0.5), 1f, new Color(black, true));

            Render2DUtil.drawRect(matrices, (float) (endPosX - 0.5), (float) (posY - 0.5), 1.5f, (float) (h4 + 0.5), new Color(black, true));
            Render2DUtil.drawRect(matrices, (float) (endPosX - 0.5), (float) (endPosY - h4 - 0.5), 1.5f, (float) (h4 + 0.5), new Color(black, true));
            Render2DUtil.drawRect(matrices, (float) (posX - 1), (float) (endPosY - 0.5), (float) (w3 + 1.5), 1f, new Color(black, true));
            Render2DUtil.drawRect(matrices, (float) (endPosX - w3 - 0.5), (float) (endPosY - 0.5), (float) (w3 + 0.5), 1f, new Color(black, true));

            Render2DUtil.drawRect(matrices, (float) (posX - 0.5), (float) posY, 0.5f, (float) h4, new Color(color, true));
            Render2DUtil.drawRect(matrices, (float) (posX - 0.5), (float) (endPosY - h4), 0.5f, (float) h4, new Color(color, true));
            Render2DUtil.drawRect(matrices, (float) (posX - 0.5), (float) posY, (float) (w3 + 0.5), 0.5f, new Color(color, true));
            Render2DUtil.drawRect(matrices, (float) (endPosX - w3), (float) posY, (float) (w3 + 0.5), 0.5f, new Color(color, true));

            Render2DUtil.drawRect(matrices, (float) (endPosX - 0.5), (float) posY, 0.5f, (float) h4, new Color(color, true));
            Render2DUtil.drawRect(matrices, (float) (endPosX - 0.5), (float) (endPosY - h4), 0.5f, (float) h4, new Color(color, true));
            Render2DUtil.drawRect(matrices, (float) (posX - 0.5), (float) (endPosY - 0.5), (float) (w3 + 0.5), 0.5f, new Color(color, true));
            Render2DUtil.drawRect(matrices, (float) (endPosX - w3), (float) (endPosY - 0.5), (float) (w3 - 0.5), 0.5f, new Color(color, true));
        }
    }

    private void drawHealthBar(DrawContext context, LivingEntity living, double posX, double posY, double endPosX, double endPosY) {
        float health = living.getHealth();
        float maxHealth = living.getMaxHealth();

        if (bbtt.getValue() && living instanceof PlayerEntity) {
            health = living.getHealth() + living.getAbsorptionAmount();
            maxHealth = living.getMaxHealth() + 16.0f;
        }

        if (health > maxHealth) health = maxHealth;

        double durabilityWidth = health / maxHealth;
        double textWidth = (endPosY - posY) * durabilityWidth;

        int healthColor = getHealthColor(health, maxHealth).getRGB();
        int bgColor = new Color(0, 0, 0, 120).getRGB();

        if (healthNumber.getValue()) {
            float scale = fontScale.getValue();
            float drawY = (float) (endPosY - textWidth - FontRenderers.ducksans.getStringHeight(scale) / 2.0f);

            if (hpMode.getValue() == HpMode.Health) {
                String healthNum = dFormat.format(living.getHealth() + living.getAbsorptionAmount());
                float numW = FontRenderers.ducksans.getStringWidth(healthNum, scale);
                FontRenderers.ducksans.drawText(context.getMatrices(), healthNum, (float) (posX - 4 - numW) - 9, drawY + 3.2f, scale, new Color(healthColor, true));

                context.getMatrices().push();
                context.getMatrices().translate(posX - 4, drawY, 0);
                context.getMatrices().scale(scale, scale, 1.0f);
                context.drawText(mc.textRenderer, "§c❤", - 9, 5, 0xFFFF0000, false);
                context.getMatrices().pop();
            } else {
                String healthPercent = (int) (living.getHealth() / maxHealth * 100.0f) + "%";
                float textW = FontRenderers.ducksans.getStringWidth(healthPercent, scale);
                FontRenderers.ducksans.drawText(context.getMatrices(), healthPercent, (float) (posX - 4 - textW), drawY, scale, new Color(healthColor, true));
            }
        }

        Render2DUtil.drawRect(context.getMatrices(), (float) (posX - 3.5), (float) (posY - 0.5), 2f, (float) (endPosY - posY + 1), new Color(bgColor, true));

        if (health > 0) {
            double deltaY = endPosY - posY;

            if (hpBarMode.getValue() == HpBarMode.Dot && deltaY >= 60.0) {
                for (int k = 0; k < 10; k++) {
                    double reratio = MathHelper.clamp(
                            health - k * (maxHealth / 10.0f), 0, maxHealth / 10.0f
                    ) / (maxHealth / 10.0f);
                    double hei = (deltaY / 10.0 - 0.5) * reratio;

                    float rectX = (float) (posX - 3);
                    float rectY = (float) (endPosY - (deltaY + 0.5) / 10.0 * k - hei);
                    float rectW = 1f;
                    float rectH = (float) hei;

                    Render2DUtil.drawRect(context.getMatrices(), rectX, rectY, rectW, rectH, new Color(healthColor, true));
                }
            } else {
                Render2DUtil.drawRect(context.getMatrices(), (float) (posX - 3), (float) (endPosY - textWidth), 1f, (float) textWidth, new Color(healthColor, true));
            }
        }
    }

    private void drawNameTag(DrawContext context, PlayerEntity player, double posX, double posY, double endPosX, double endPosY) {
        String name = player.getName().getString();
        boolean isFriend = Managers.FRIEND.isFriend(player);
        Color nameColor = isFriend ? friendColor.getValue() : Color.WHITE;

        float scale = fontScale.getValue();
        float textW = FontRenderers.ducksans.getStringWidth(name, scale);
        float textH = FontRenderers.ducksans.getStringHeight(scale);
        float centerX = (float) (posX + (endPosX - posX) / 2.0);

        if (tagsBG.getValue()) {
            float bgW = textW + 4 * scale;
            float bgH = textH + 4 * scale;
            Render2DUtil.drawRect(context.getMatrices(), centerX - bgW / 2, (float) (posY - 1 - bgH), bgW, bgH, new Color(0xA0000000, true));
        }

        FontRenderers.ducksans.drawText(context.getMatrices(), name, centerX - textW / 2, (float) (posY - 1 - textH), scale, nameColor);
    }

    private void drawArmorBar(net.minecraft.client.util.math.MatrixStack matrices, PlayerEntity player, double posX, double posY, double endPosX, double endPosY) {
        double constHeight = (endPosY - posY) / 4.0;

        for (int m = 4; m > 0; m--) {
            ItemStack armorStack = player.getInventory().armor.get(m - 1);
            double theHeight = constHeight + 0.25;

            float barX = (float) (endPosX + 1.5);
            float barW = 2f;
            float barY1 = (float) (endPosY + 0.5 - theHeight * m);
            float barY2 = (float) (endPosY + 0.5 - theHeight * (m - 1));
            float barH = barY2 - barY1;

            Render2DUtil.drawRect(matrices, barX, barY1, barW, barH, new Color(0, 0, 0, 120));

            if (!armorStack.isEmpty() && armorStack.getMaxDamage() > 0) {
                float durability = (float) armorStack.getDamage() / armorStack.getMaxDamage();
                float ratio = MathHelper.clamp(1.0f - durability, 0.0f, 1.0f);
                float durH = (float) ((constHeight - 0.25) * ratio);
                Render2DUtil.drawRect(matrices, barX + 0.5f, barY2 - 0.25f - durH, 1f, durH, Color.CYAN);
            }
        }
    }

    private void drawArmorItems(DrawContext context, PlayerEntity player, double posX, double posY, double endPosX, double endPosY) {
        double yDist = (endPosY - posY) / 4.0;

        for (int m = 4; m > 0; m--) {
            ItemStack armorStack = player.getInventory().armor.get(m - 1);
            float itemX = (float) (endPosX + (armorBar.getValue() ? 4.0 : 2.0));
            float itemY = (float) (posY + yDist * (4 - m) + yDist / 2.0 - 8);

            if (!armorStack.isEmpty()) {
                context.getMatrices().push();
                context.getMatrices().translate(itemX, itemY, 0);
                float s = 0.5f;
                context.getMatrices().scale(s, s, 1.0f);
                context.drawItem(armorStack, 0, 0);
                context.getMatrices().pop();

                if (armorDur.getValue() && armorStack.getMaxDamage() > 0) {
                    int dur = armorStack.getMaxDamage() - armorStack.getDamage();
                    String durText = String.valueOf(dur);
                    float smallScale = fontScale.getValue() * 0.8f;
                    float tw = FontRenderers.ducksans.getStringWidth(durText, smallScale);
                    FontRenderers.ducksans.drawText(context.getMatrices(), durText, itemX + 4.5f - tw / 2, itemY + 8, smallScale, Color.WHITE);
                }
            }
        }
    }

    private void drawItemTags(DrawContext context, LivingEntity living, double posX, double posY, double endPosX, double endPosY) {
        if (itemRender.getValue()) {
            ItemStack mainHand = living.getMainHandStack();
            ItemStack offHand = living.getOffHandStack();

            if (!mainHand.isEmpty()) {
                context.getMatrices().push();
                context.getMatrices().translate(posX, endPosY, 0);
                context.getMatrices().scale(0.5f, 0.5f, 1.0f);
                context.drawItem(mainHand, 0, 0);
                context.getMatrices().pop();
            }

            if (!offHand.isEmpty()) {
                float offset = mainHand.isEmpty() ? 0 : 8;
                context.getMatrices().push();
                context.getMatrices().translate(posX + offset, endPosY, 0);
                context.getMatrices().scale(0.5f, 0.5f, 1.0f);
                context.drawItem(offHand, 0, 0);
                context.getMatrices().pop();
            }
        } else {
            ItemStack mainHand = living.getMainHandStack();
            if (!mainHand.isEmpty()) {
                String itemName = mainHand.getName().getString();
                float scale = fontScale.getValue();
                float textW = FontRenderers.ducksans.getStringWidth(itemName, scale);
                float textH = FontRenderers.ducksans.getStringHeight(scale);
                float centerX = (float) (posX + (endPosX - posX) / 2.0);

                if (tagsBG.getValue()) {
                    float bgW = textW + 4 * scale;
                    float bgH = textH + 4 * scale;
                    Render2DUtil.drawRect(context.getMatrices(), centerX - bgW / 2, (float) (endPosY + 1 - 2 * scale), bgW, bgH, new Color(0xA0000000, true));
                }

                FontRenderers.ducksans.drawText(context.getMatrices(), itemName, centerX - textW / 2, (float) (endPosY + 1), scale, Color.WHITE);
            }

            ItemStack offHand = living.getOffHandStack();
            if (!offHand.isEmpty()) {
                String offName = offHand.getName().getString();
                float offY = 7.5f;
                float scale = fontScale.getValue();
                float textW = FontRenderers.ducksans.getStringWidth(offName, scale);
                float textH = FontRenderers.ducksans.getStringHeight(scale);
                float centerX = (float) (posX + (endPosX - posX) / 2.0);

                if (tagsBG.getValue()) {
                    float bgW = textW + 4 * scale;
                    float bgH = textH + 4 * scale;
                    Render2DUtil.drawRect(context.getMatrices(), centerX - bgW / 2, (float) (endPosY + offY - 2 * scale), bgW, bgH, new Color(0xA0000000, true));
                }

                FontRenderers.ducksans.drawText(context.getMatrices(), offName, centerX - textW / 2, (float) (endPosY + offY), scale, Color.WHITE);
            }
        }
    }

    private List<Entity> collectEntities() {
        List<Entity> collected = new ArrayList<>();
        float rangeSq = range.getValue() * range.getValue();
        for (Entity entity : mc.world.getEntities()) {
            if (mc.player.squaredDistanceTo(entity.getPos()) > rangeSq) continue;
            if (isSelected(entity)) {
                collected.add(entity);
            }
        }
        return collected;
    }

    private boolean isSelected(Entity entity) {
        if (entity == mc.player) return self.getValue() && !mc.options.getPerspective().isFirstPerson();
        if (entity instanceof EndCrystalEntity) return crystals.getValue();
        if (entity instanceof ItemEntity) return droppedItems.getValue();
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) return false;

        if (entity instanceof PlayerEntity) {
            return players.getValue();
        }
        if (entity instanceof MobEntity) {
            return mobs.getValue();
        }
        if (entity instanceof PassiveEntity) {
            return animals.getValue();
        }
        return false;
    }

    public Color getColor(Entity entity) {
        if (entity instanceof PlayerEntity && Managers.FRIEND.isFriend((PlayerEntity) entity)) {
            return friendColor.getValue();
        }
        switch (colorMode.getValue()) {
            case Custom:
                return customColor.getValue();
            case AnotherRainbow:
                return new Color(getRainbowOpaque(mixerSeconds.getValue(), saturation.getValue(), brightness.getValue(), 0));
            case Slowly:
                return slowlyRainbow(System.nanoTime(), 0, saturation.getValue(), brightness.getValue());
            default:
                return fade(customColor.getValue(), 0, 100);
        }
    }

    public static Color getHealthColor(float health, float maxHealth) {
        float[] fractions = {0.0f, 0.5f, 1.0f};
        Color[] colors = {new Color(108, 0, 0), new Color(255, 51, 0), Color.GREEN};
        float progress = health / maxHealth;
        return blendColors(fractions, colors, progress).brighter();
    }

    public static Color blendColors(float[] fractions, Color[] colors, float progress) {
        int[] indices = getFractionIndices(fractions, progress);
        float[] range = {fractions[indices[0]], fractions[indices[1]]};
        Color[] colorRange = {colors[indices[0]], colors[indices[1]]};
        float max = range[1] - range[0];
        float value = progress - range[0];
        float weight = value / max;
        return blend(colorRange[0], colorRange[1], 1.0f - weight);
    }

    public static Color blend(Color c1, Color c2, float ratio) {
        float ir = 1.0f - ratio;
        float r = MathHelper.clamp(c1.getRed() / 255f * ratio + c2.getRed() / 255f * ir, 0, 1);
        float g = MathHelper.clamp(c1.getGreen() / 255f * ratio + c2.getGreen() / 255f * ir, 0, 1);
        float b = MathHelper.clamp(c1.getBlue() / 255f * ratio + c2.getBlue() / 255f * ir, 0, 1);
        return new Color(r, g, b);
    }

    public static int[] getFractionIndices(float[] fractions, float progress) {
        int startPoint = 0;
        while (startPoint < fractions.length && fractions[startPoint] <= progress) {
            startPoint++;
        }
        if (startPoint >= fractions.length) startPoint = fractions.length - 1;
        return new int[]{startPoint - 1, startPoint};
    }

    public static Color fade(Color color, int index, int count) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float b = Math.abs(((float) (System.currentTimeMillis() % 2000L) / 1000.0f + (float) index / (float) count * 2.0f) % 2.0f - 1.0f);
        b = 0.5f + 0.5f * b;
        hsb[2] = b % 2.0f;
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    public static Color slowlyRainbow(long time, int count, float sat, float bri) {
        Color color = new Color(Color.HSBtoRGB(((float) time + (float) count * -3000000.0f) / 2.0f / 1.0E9f, sat, bri));
        return new Color((float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f, (float) color.getBlue() / 255.0f, (float) color.getAlpha() / 255.0f);
    }

    public static int getRainbowOpaque(int seconds, float saturation, float brightness, int index) {
        float hue = (float) ((System.currentTimeMillis() + (long) index) % (long) (seconds * 1000L)) / (float) (seconds * 1000);
        return Color.HSBtoRGB(hue, saturation, brightness);
    }
}
