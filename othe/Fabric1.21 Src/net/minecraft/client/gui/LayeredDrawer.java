/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

@Environment(value=EnvType.CLIENT)
public class LayeredDrawer {
    public static final float LAYER_Z_PADDING = 200.0f;
    private final List<Layer> layers = new ArrayList<Layer>();

    public LayeredDrawer addLayer(Layer layer) {
        this.layers.add(layer);
        return this;
    }

    public LayeredDrawer addSubDrawer(LayeredDrawer drawer, BooleanSupplier shouldRender) {
        return this.addLayer((context, tickCounter) -> {
            if (shouldRender.getAsBoolean()) {
                drawer.renderInternal(context, tickCounter);
            }
        });
    }

    public void render(DrawContext context, RenderTickCounter tickCounter) {
        context.getMatrices().push();
        this.renderInternal(context, tickCounter);
        context.getMatrices().pop();
    }

    private void renderInternal(DrawContext context, RenderTickCounter tickCounter) {
        for (Layer layer : this.layers) {
            layer.render(context, tickCounter);
            context.getMatrices().translate(0.0f, 0.0f, 200.0f);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Layer {
        public void render(DrawContext var1, RenderTickCounter var2);
    }
}

