/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.RabbitEntityModel;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class RabbitEntityRenderer
extends MobEntityRenderer<RabbitEntity, RabbitEntityModel<RabbitEntity>> {
    private static final Identifier BROWN_TEXTURE = Identifier.ofVanilla("textures/entity/rabbit/brown.png");
    private static final Identifier WHITE_TEXTURE = Identifier.ofVanilla("textures/entity/rabbit/white.png");
    private static final Identifier BLACK_TEXTURE = Identifier.ofVanilla("textures/entity/rabbit/black.png");
    private static final Identifier GOLD_TEXTURE = Identifier.ofVanilla("textures/entity/rabbit/gold.png");
    private static final Identifier SALT_TEXTURE = Identifier.ofVanilla("textures/entity/rabbit/salt.png");
    private static final Identifier WHITE_SPLOTCHED_TEXTURE = Identifier.ofVanilla("textures/entity/rabbit/white_splotched.png");
    private static final Identifier TOAST_TEXTURE = Identifier.ofVanilla("textures/entity/rabbit/toast.png");
    private static final Identifier CAERBANNOG_TEXTURE = Identifier.ofVanilla("textures/entity/rabbit/caerbannog.png");

    public RabbitEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new RabbitEntityModel(context.getPart(EntityModelLayers.RABBIT)), 0.3f);
    }

    @Override
    public Identifier getTexture(RabbitEntity rabbitEntity) {
        String string = Formatting.strip(rabbitEntity.getName().getString());
        if ("Toast".equals(string)) {
            return TOAST_TEXTURE;
        }
        return switch (rabbitEntity.getVariant()) {
            default -> throw new MatchException(null, null);
            case RabbitEntity.RabbitType.BROWN -> BROWN_TEXTURE;
            case RabbitEntity.RabbitType.WHITE -> WHITE_TEXTURE;
            case RabbitEntity.RabbitType.BLACK -> BLACK_TEXTURE;
            case RabbitEntity.RabbitType.GOLD -> GOLD_TEXTURE;
            case RabbitEntity.RabbitType.SALT -> SALT_TEXTURE;
            case RabbitEntity.RabbitType.WHITE_SPLOTCHED -> WHITE_SPLOTCHED_TEXTURE;
            case RabbitEntity.RabbitType.EVIL -> CAERBANNOG_TEXTURE;
        };
    }
}

