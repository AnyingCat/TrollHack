/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.ProfilesTooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.tooltip.BundleTooltipData;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public interface TooltipComponent {
    public static TooltipComponent of(OrderedText text) {
        return new OrderedTextTooltipComponent(text);
    }

    public static TooltipComponent of(TooltipData data) {
        if (data instanceof BundleTooltipData) {
            BundleTooltipData bundleTooltipData = (BundleTooltipData)data;
            return new BundleTooltipComponent(bundleTooltipData.contents());
        }
        if (data instanceof ProfilesTooltipComponent.ProfilesData) {
            ProfilesTooltipComponent.ProfilesData profilesData = (ProfilesTooltipComponent.ProfilesData)data;
            return new ProfilesTooltipComponent(profilesData);
        }
        throw new IllegalArgumentException("Unknown TooltipComponent");
    }

    public int getHeight();

    public int getWidth(TextRenderer var1);

    default public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, VertexConsumerProvider.Immediate vertexConsumers) {
    }

    default public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
    }
}

