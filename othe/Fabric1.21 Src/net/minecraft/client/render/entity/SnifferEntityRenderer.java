/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SnifferEntityModel;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class SnifferEntityRenderer
extends MobEntityRenderer<SnifferEntity, SnifferEntityModel<SnifferEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/sniffer/sniffer.png");

    public SnifferEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new SnifferEntityModel(context.getPart(EntityModelLayers.SNIFFER)), 1.1f);
    }

    @Override
    public Identifier getTexture(SnifferEntity snifferEntity) {
        return TEXTURE;
    }
}

