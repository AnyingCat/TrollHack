package github.trollhack.modules.impl.render;

import github.trollhack.core.Managers;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.utils.render.ProjectionUtil;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class NameTags extends Module {
    public static NameTags INSTANCE = new NameTags();

    private final FloatSetting height = floatSetting("Height", 0.0f, -3.0f, 3.0f, 0.01f);
    private final FloatSetting scale = floatSetting("Scale", 1.0f, 0.5f, 3.0f, 0.1f);
    private final BooleanSetting self = booleanSetting("Self", true);
    private final BooleanSetting health = booleanSetting("Health", true);
    private final BooleanSetting ping = booleanSetting("Ping", true);
    private final BooleanSetting distance = booleanSetting("Distance", true);
    private final BooleanSetting gamemode = booleanSetting("Gamemode", false);
    private final BooleanSetting armor = booleanSetting("Armor", true);

    public NameTags() {
        super("NameTags", Category.RENDER);
    }

    @Override
    public void onRender2D(DrawContext context) {
        if (nullCheck()) return;

        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player && !self.getValue()) continue;
            if (!player.isAlive()) continue;

            double x = MathHelper.lerp(tickDelta, player.prevX, player.getX());
            double y = MathHelper.lerp(tickDelta, player.prevY, player.getY()) + height.getValue() + player.getBoundingBox().getLengthY() + 0.3;
            double z = MathHelper.lerp(tickDelta, player.prevZ, player.getZ());

            Vec3d worldPos = new Vec3d(x, y, z);
            Vec3d screenPos = ProjectionUtil.worldToScreen(worldPos);

            if (screenPos == null || screenPos.z <= 0 || screenPos.z >= 1) continue;

            float screenX = (float) screenPos.x;
            float screenY = (float) screenPos.y;

            if (screenX < -64 || screenY < -64 ||
                screenX > mc.getWindow().getScaledWidth() + 64 ||
                screenY > mc.getWindow().getScaledHeight() + 64) continue;

            float dist = MathHelper.sqrt((float) mc.player.squaredDistanceTo(worldPos));
            float baseScale = scale.getValue();
            float scaleFactor;
            if (dist <= 1.0f) {
                scaleFactor = baseScale;
            } else {
                float normalizedDist = MathHelper.clamp(dist / 64.0f, 0.0f, 1.0f);
                scaleFactor = Math.max(baseScale * (1.0f - normalizedDist * 0.5f), 0.5f * baseScale);
            }

            PlayerListEntry entry = mc.getNetworkHandler() != null
                ? mc.getNetworkHandler().getPlayerListEntry(player.getUuid())
                : null;
            int pingValue = entry != null ? entry.getLatency() : -1;
            GameMode gamemodeValue = entry != null ? entry.getGameMode() : null;
            float hp = player.getHealth() + player.getAbsorptionAmount();
            double distanceValue = mc.player.distanceTo(player);

            StringBuilder fullTextBuilder = new StringBuilder();
            if (ping.getValue()) fullTextBuilder.append(pingValue).append("ms ");
            if (gamemode.getValue()) {
                if (gamemodeValue == null) fullTextBuilder.append("[BOT] ");
                else fullTextBuilder.append(switch (gamemodeValue) {
                    case SURVIVAL -> "[S] ";
                    case CREATIVE -> "[C] ";
                    case ADVENTURE -> "[A] ";
                    case SPECTATOR -> "[SP] ";
                });
            }
            fullTextBuilder.append(player.getName().getString());
            if (health.getValue()) fullTextBuilder.append(" ").append(roundToOneDecimal(hp));
            if (distance.getValue()) fullTextBuilder.append(" ").append(String.format("%.1fm", distanceValue));

            float textWidth = FontRenderers.ducksans.getStringWidth(fullTextBuilder.toString(), scaleFactor);
            float textHeight = FontRenderers.ducksans.getStringHeight(scaleFactor);

            float bgWidth = textWidth + 8 * scaleFactor;
            float bgHeight = textHeight + 6 * scaleFactor;

            float tagX = screenX - bgWidth / 2.0f;
            float tagY = screenY - bgHeight - 2 * scaleFactor;

            if (armor.getValue()) {
                List<ItemStack> equipment = new ArrayList<>();
                equipment.add(player.getMainHandStack());
                for (int i = 3; i >= 0; i--) {
                    equipment.add(player.getInventory().armor.get(i));
                }
                equipment.add(player.getOffHandStack());

                float itemSize = 16 * scaleFactor;
                float itemSpacing = 2 * scaleFactor;
                float totalItemsWidth = equipment.size() * itemSize + (equipment.size() - 1) * itemSpacing;
                float startX = tagX + bgWidth / 2.0f - totalItemsWidth / 2.0f;
                float itemY = tagY - itemSize - 2 * scaleFactor;

                DiffuseLighting.disableGuiDepthLighting();

                float itemOffset = 0;
                for (ItemStack stack : equipment) {
                    if (stack.isEmpty()) {
                        itemOffset += itemSize + itemSpacing;
                        continue;
                    }

                    float itemX = startX + itemOffset;

                    context.getMatrices().push();
                    context.getMatrices().translate(itemX, itemY, 0);
                    context.getMatrices().scale(scaleFactor, scaleFactor, 1.0f);
                    context.drawItem(stack, 0, 0);
                    context.getMatrices().pop();

                    if (stack.getMaxDamage() > 0) {
                        float durabilityPercent = 1.0f - (float) stack.getDamage() / stack.getMaxDamage();
                        int percent = (int) (durabilityPercent * 100);
                        Color durColor = percent <= 33 ? Color.RED : percent <= 66 ? Color.ORANGE : Color.GREEN;

                        String percentText = String.valueOf(percent);
                        float smallScale = scaleFactor * 0.6f;
                        float textW = FontRenderers.ducksans.getStringWidth(percentText, smallScale);
                        float textPosX = itemX + itemSize / 2.0f - textW / 2.0f;
                        float textPosY = itemY - FontRenderers.ducksans.getStringHeight(smallScale) - 1;
                        FontRenderers.ducksans.drawText(context.getMatrices(), percentText, textPosX, textPosY, smallScale, durColor);
                    }

                    itemOffset += itemSize + itemSpacing;
                }
            }

            Render2DUtil.drawRect(context.getMatrices(), tagX, tagY, bgWidth, bgHeight, new Color(0x99000001, true));

            float healthPercent = MathHelper.clamp(hp / (player.getMaxHealth() + player.getAbsorptionAmount()), 0, 1);
            Color healthBarColor = getHealthBarColor(healthPercent);
            float healthBarY = tagY + bgHeight;
            Render2DUtil.drawRect(context.getMatrices(), tagX, healthBarY, bgWidth, 1.5f * scaleFactor, new Color(0x80000000, true));
            Render2DUtil.drawRect(context.getMatrices(), tagX, healthBarY, bgWidth * healthPercent, 1.5f * scaleFactor, healthBarColor);

            float textX = tagX + 4 * scaleFactor;
            float textY = tagY + 3 * scaleFactor;
            float currentX = textX;

            if (ping.getValue()) {
                String pingText = pingValue + "ms ";
                Color pingColor = pingValue < 0 ? Color.GRAY : pingValue < 100 ? Color.GREEN : pingValue < 250 ? Color.YELLOW : Color.RED;
                FontRenderers.ducksans.drawText(context.getMatrices(), pingText, currentX, textY, scaleFactor, pingColor);
                currentX += FontRenderers.ducksans.getStringWidth(pingText, scaleFactor);
            }

            if (gamemode.getValue()) {
                String gmText;
                Color gmColor;
                if (gamemodeValue == null) {
                    gmText = "[BOT] ";
                    gmColor = Color.GRAY;
                } else {
                    gmText = switch (gamemodeValue) {
                        case SURVIVAL -> "[S] ";
                        case CREATIVE -> "[C] ";
                        case ADVENTURE -> "[A] ";
                        case SPECTATOR -> "[SP] ";
                    };
                    gmColor = switch (gamemodeValue) {
                        case SURVIVAL -> Color.CYAN;
                        case CREATIVE -> Color.RED;
                        case ADVENTURE -> Color.YELLOW;
                        case SPECTATOR -> Color.GRAY;
                    };
                }
                FontRenderers.ducksans.drawText(context.getMatrices(), gmText, currentX, textY, scaleFactor, gmColor);
                currentX += FontRenderers.ducksans.getStringWidth(gmText, scaleFactor);
            }

            String nameText = player.getName().getString();
            boolean isFriend = Managers.FRIEND.isFriend(player);
            Color nameColor = isFriend ? new Color(0, 255, 0) : Color.WHITE;
            FontRenderers.ducksans.drawText(context.getMatrices(), nameText, currentX, textY, scaleFactor, nameColor);
            currentX += FontRenderers.ducksans.getStringWidth(nameText, scaleFactor);

            if (health.getValue()) {
                String healthText = " " + roundToOneDecimal(hp);
                FontRenderers.ducksans.drawText(context.getMatrices(), healthText, currentX, textY, scaleFactor, healthBarColor);
                currentX += FontRenderers.ducksans.getStringWidth(healthText, scaleFactor);
            }

            if (distance.getValue()) {
                String distText = " " + String.format("%.1fm", distanceValue);
                FontRenderers.ducksans.drawText(context.getMatrices(), distText, currentX, textY, scaleFactor, Color.WHITE);
            }
        }
    }

    private Color getHealthBarColor(float percent) {
        if (percent > 0.6f) return new Color(0x9900FF00, true);
        if (percent > 0.3f) return new Color(0x99EEFF05, true);
        return new Color(0x99FF0000, true);
    }

    private float roundToOneDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).floatValue();
    }
}
