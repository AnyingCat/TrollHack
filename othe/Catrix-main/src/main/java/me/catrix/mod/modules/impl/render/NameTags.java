package me.catrix.mod.modules.impl.render;

import me.catrix.Catrix;
import me.catrix.api.utils.entity.EntityUtil;
import me.catrix.api.utils.render.Render2DUtil;
import me.catrix.api.utils.render.TextUtil;
import me.catrix.mod.gui.font.FontRenderers;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.impl.player.Freecam;
import me.catrix.mod.modules.settings.impl.BooleanSetting;
import me.catrix.mod.modules.settings.impl.ColorSetting;
import me.catrix.mod.modules.settings.impl.EnumSetting;
import me.catrix.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4d;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class NameTags extends Module {
    public static NameTags INSTANCE;
    private final SliderSetting scale = add(new SliderSetting("Scale", 0.91f, 0.1f, 2f, 0.01));
    private final SliderSetting minScale = add(new SliderSetting("MinScale", 0.2f, 0.1f, 1f, 0.01));
    private final SliderSetting scaled = add(new SliderSetting("Scaled", 1, 0, 2, 0.01));
    private final SliderSetting offset = add(new SliderSetting("Offset", 0.315f, 0.001f, 1f, 0.001));
    private final SliderSetting height = add(new SliderSetting("Height", 0, -3, 3, 0.01));
    private final BooleanSetting god = add(new BooleanSetting("God", true));
    private final BooleanSetting gamemode = add(new BooleanSetting("Gamemode", false));
    private final BooleanSetting hp = add(new BooleanSetting("HP", true));
    private final BooleanSetting ping = add(new BooleanSetting("Ping", true));
    private final BooleanSetting distance = add(new BooleanSetting("Distance", true));
    private final BooleanSetting pops = add(new BooleanSetting("TotemPops", true));
    private final BooleanSetting enchants = add(new BooleanSetting("Enchants", true));
    private final ColorSetting rect = add(new ColorSetting("Rect", new Color(0x99000001, true)).injectBoolean(true));
    private final ColorSetting friendColor = add(new ColorSetting("FriendColor", new Color(0xFF1DFF1D, true)));
    private final ColorSetting color = add(new ColorSetting("Color", new Color(0xFFFFFFFF, true)));
    private final SliderSetting armorHeight = add(new SliderSetting("ArmorHeight", -3.2f, -10, 10f));
    private final SliderSetting armorScale = add(new SliderSetting("ArmorScale", 0.9f, 0.1f, 2f, 0.01f));
    public final EnumSetting<Font> font = add(new EnumSetting<>("FontMode", Font.Fancy));
    private final EnumSetting<Armor> armorMode = add(new EnumSetting<>("ArmorMode", Armor.Full));

    public NameTags() {
        super("NameTags", Category.Render);
        setChinese("名字标签");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        for (PlayerEntity ent : mc.world.getPlayers()) {
            if (ent == mc.player && mc.options.getPerspective().isFirstPerson() && Freecam.INSTANCE.isOff()) continue;

            double x = MathHelper.lerp(mc.getTickDelta(), ent.prevX, ent.getX());
            double y = MathHelper.lerp(mc.getTickDelta(), ent.prevY, ent.getY()) + height.getValue() + ent.getBoundingBox().getLengthY() + 0.3;
            double z = MathHelper.lerp(mc.getTickDelta(), ent.prevZ, ent.getZ());
            Vec3d preVec = new Vec3d(x, y, z);
            Vec3d vector = TextUtil.worldSpaceToScreenSpace(preVec);

            if (vector.z > 0 && vector.z < 1) {
                Vector4d position = new Vector4d(vector.x, vector.y, vector.z, 0);
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);

                StringBuilder finalStringBuilder = new StringBuilder();
                if (god.getValue() && ent.hasStatusEffect(StatusEffects.SLOWNESS)) {
                    finalStringBuilder.append("§4GOD ");
                }
                if (ping.getValue()) {
                    finalStringBuilder.append(getEntityPing(ent)).append("ms ");
                }
                if (gamemode.getValue()) {
                    finalStringBuilder.append(translateGamemode(getEntityGamemode(ent))).append(" ");
                }
                finalStringBuilder.append(Formatting.RESET).append(ent.getName().getString());
                if (hp.getValue()) {
                    finalStringBuilder.append(" ").append(getHealthColor(ent)).append(round2(ent.getAbsorptionAmount() + ent.getHealth()));
                }
                if (distance.getValue()) {
                    finalStringBuilder.append(" ").append(Formatting.RESET).append(String.format("%.1f", mc.player.distanceTo(ent))).append("m");
                }
                if (pops.getValue() && Catrix.POP.getPop(ent.getName().getString()) != 0) {
                    finalStringBuilder.append(" §bPop").append(" ").append(Formatting.LIGHT_PURPLE).append(Catrix.POP.getPop(ent.getName().getString()));
                }
                String final_string = finalStringBuilder.toString();

                double posX = position.x;
                double posY = position.y;
                double endPosX = position.z;

                float diff = (float) (endPosX - posX) / 2;
                float textWidth = font.getValue() == Font.Fancy ? FontRenderers.ui.getWidth(final_string) : mc.textRenderer.getWidth(final_string);
                float tagX = (float) (posX + diff - textWidth / 2);

                ArrayList<ItemStack> stacks = new ArrayList<>();
                stacks.add(ent.getMainHandStack());
                for (int i = 3; i >= 0; i--) {
                    stacks.add(ent.getInventory().armor.get(i));
                }
                stacks.add(ent.getOffHandStack());

                context.getMatrices().push();
                context.getMatrices().translate(tagX - 2 + (textWidth + 4) / 2f, (float) (posY - 13f) + 6.5f, 0);
                float size = (float) Math.max(1 - MathHelper.sqrt((float) mc.cameraEntity.squaredDistanceTo(preVec)) * 0.01 * scaled.getValue(), 0);
                float scaleFactor = Math.max(scale.getValueFloat() * size, minScale.getValueFloat());
                context.getMatrices().scale(scaleFactor, scaleFactor, 1f);
                context.getMatrices().translate(0, offset.getValueFloat() * MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(preVec)), 0);
                context.getMatrices().translate(-(tagX - 2 + (textWidth + 4) / 2f), -(float) ((posY - 13f) + 6.5f), 0);

                if (armorMode.getValue() != Armor.None) {
                    float item_offset = 0;
                    for (ItemStack armorComponent : stacks) {
                        if (!armorComponent.isEmpty()) {
                            context.getMatrices().push();
                            context.getMatrices().translate(tagX - 2 + (textWidth + 4) / 2f, (float) (posY - 13f) + 6.5f, 0);
                            context.getMatrices().scale(armorScale.getValueFloat(), armorScale.getValueFloat(), 1f);
                            context.getMatrices().translate(-(tagX - 2 + (textWidth + 4) / 2f), -(float) ((posY - 13f) + 6.5f), 0);
                            context.getMatrices().translate(posX - 52.5 + item_offset, (float) (posY - 29f) + armorHeight.getValueFloat(), 0);

                            float durability = armorComponent.getMaxDamage() - armorComponent.getDamage();
                            int percent = (int) ((durability / (float) armorComponent.getMaxDamage()) * 100F);
                            Color color = percent <= 33 ? Color.RED : percent <= 66 ? Color.ORANGE : Color.GREEN;

                            DiffuseLighting.disableGuiDepthLighting();
                            boolean shouldRenderItem = true;
                            boolean shouldRenderDurability = false;
                            boolean shouldRenderBar = false;

                            switch (armorMode.getValue()) {
                                case OnlyArmor -> {
                                    int index = stacks.indexOf(armorComponent);
                                    if (index <= 1 || index > 5) shouldRenderItem = false;
                                }
                                case Full -> {
                                    shouldRenderDurability = true;
                                }
                                case Durability -> {
                                    shouldRenderItem = false;
                                    shouldRenderBar = true;
                                    shouldRenderDurability = true;
                                }
                            }

                            if (shouldRenderItem) {
                                context.drawItem(armorComponent, 0, 0);
                                context.drawItemInSlot(mc.textRenderer, armorComponent, 0, 0);
                            }

                            if (shouldRenderBar && armorComponent.getMaxDamage() > 0 && !armorComponent.isItemBarVisible()) {
                                int i = armorComponent.getItemBarStep();
                                int j = armorComponent.getItemBarColor();
                                context.fill(RenderLayer.getGuiOverlay(), 2, 13, 2 + 13, 13 + 2, -16777216);
                                context.fill(RenderLayer.getGuiOverlay(), 2, 13, 2 + i, 13 + 1, j | -16777216);
                            }

                            if (shouldRenderDurability && armorComponent.getMaxDamage() > 0) {
                                if (font.getValue() == Font.Fancy) {
                                    FontRenderers.ui.drawString(context.getMatrices(), String.valueOf(percent), 9 - FontRenderers.ui.getWidth(String.valueOf(percent)) / 2,
                                            armorMode.getValue() == Armor.Full ? -FontRenderers.ui.getFontHeight() + 3 : 7, color.getRGB());
                                } else {
                                    context.drawText(mc.textRenderer, String.valueOf(percent), 9 - mc.textRenderer.getWidth(String.valueOf(percent)) / 2,
                                            armorMode.getValue() == Armor.Full ? -mc.textRenderer.fontHeight + 1 : 5, color.getRGB(), true);
                                }
                            }

                            context.getMatrices().pop();

                            if (this.enchants.getValue()) {
                                float enchantmentY = 0;
                                NbtList enchants = armorComponent.getEnchantments();
                                for (int index = 0; index < enchants.size(); ++index) {
                                    String id = enchants.getCompound(index).getString("id");
                                    short level = enchants.getCompound(index).getShort("lvl");
                                    String encName = switch (id) {
                                        case "minecraft:blast_protection" -> "B" + level;
                                        case "minecraft:protection" -> "P" + level;
                                        case "minecraft:thorns" -> "T" + level;
                                        case "minecraft:sharpness" -> "S" + level;
                                        case "minecraft:efficiency" -> "E" + level;
                                        case "minecraft:unbreaking" -> "U" + level;
                                        case "minecraft:power" -> "PO" + level;
                                        default -> null;
                                    };
                                    if (encName == null) continue;

                                    if (font.getValue() == Font.Fancy) {
                                        FontRenderers.ui.drawString(context.getMatrices(), encName, posX - 50 + item_offset, (float) posY - 45 + enchantmentY, -1);
                                    } else {
                                        context.getMatrices().push();
                                        context.getMatrices().translate((posX - 50f + item_offset), (posY - 45f + enchantmentY), 0);
                                        context.drawText(mc.textRenderer, encName, 0, 0, -1, true);
                                        context.getMatrices().pop();
                                    }
                                    enchantmentY -= 8;
                                }
                            }
                        }
                        item_offset += 18f;
                    }
                }
                if (rect.booleanValue) {
                    Render2DUtil.drawRect(context.getMatrices(), tagX - 2, (float) (posY - 13f), textWidth + 4, 11, rect.getValue());
                }
                Render2DUtil.drawRect(context.getMatrices(), tagX - 2, (float) (posY - 2f), textWidth + 4, 1.5f, new Color(0x80000000, true));
                Render2DUtil.drawRect(context.getMatrices(), tagX - 2, (float) (posY - 2f), (textWidth + 4) * Math.max(0, Math.min(1, (ent.getHealth() + ent.getAbsorptionAmount()) / (ent.getMaxHealth() + ent.getAbsorptionAmount()))), 1.5f,
                        Math.max(0, Math.min(1, (ent.getHealth() + ent.getAbsorptionAmount()) / (ent.getMaxHealth() + ent.getAbsorptionAmount()))) > 0.6f ? new Color(0x9900FF00, true) :
                                Math.max(0, Math.min(1, (ent.getHealth() + ent.getAbsorptionAmount()) / (ent.getMaxHealth() + ent.getAbsorptionAmount()))) > 0.3f ? new Color(0x99EEFF05, true) :
                                        new Color(0x99FF0000, true));
                int textColor = Catrix.FRIEND.isFriend(ent) ? friendColor.getValue().getRGB() : this.color.getValue().getRGB();
                if (font.getValue() == Font.Fancy) {
                    FontRenderers.ui.drawString(context.getMatrices(), final_string, tagX, (float) posY - 10, textColor);
                } else {
                    context.getMatrices().push();
                    context.getMatrices().translate(tagX, ((float) posY - 11), 0);
                    context.drawText(mc.textRenderer, final_string, 0, 0, textColor, true);
                    context.getMatrices().pop();
                }
                context.getMatrices().pop();
            }
        }
    }

    public static String getEntityPing(PlayerEntity entity) {
        if (mc.getNetworkHandler() == null) return "-1";
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        if (playerListEntry == null) return "-1";
        int ping = playerListEntry.getLatency();
        Formatting color = ping >= 250 ? Formatting.RED : ping >= 100 ? Formatting.YELLOW : Formatting.GREEN;
        return color.toString() + ping;
    }

    public static GameMode getEntityGamemode(PlayerEntity entity) {
        if (entity == null) return null;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        return playerListEntry == null ? null : playerListEntry.getGameMode();
    }

    private String translateGamemode(GameMode gamemode) {
        if (gamemode == null) return "§7[BOT]";
        return switch (gamemode) {
            case SURVIVAL -> "§b[S]";
            case CREATIVE -> "§c[C]";
            case SPECTATOR -> "§7[SP]";
            case ADVENTURE -> "§e[A]";
        };
    }

    private Formatting getHealthColor(@NotNull PlayerEntity entity) {
        float maxHealth = entity.getMaxHealth() + entity.getAbsorptionAmount();
        float currentHealth = entity.getHealth() + entity.getAbsorptionAmount();
        float healthPercent = Math.max(0, Math.min(1, currentHealth / maxHealth));
        if (healthPercent > 0.6f) {
            return Formatting.GREEN;
        } else if (healthPercent > 0.3f) {
            return Formatting.YELLOW;
        } else if (healthPercent > 0.1f) {
            return Formatting.RED;
        } else {
            return Formatting.DARK_RED;
        }
    }

    public static float round2(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).floatValue();
    }

    public enum Font {
        Fancy, Fast
    }

    public enum Armor {
        None, Full, Durability, Item, OnlyArmor
    }
}