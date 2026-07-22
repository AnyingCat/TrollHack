/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.TurtleEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class TurtleEntityRenderer
extends MobEntityRenderer<TurtleEntity, TurtleEntityModel<TurtleEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/turtle/big_sea_turtle.png");

    public TurtleEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new TurtleEntityModel(context.getPart(EntityModelLayers.TURTLE)), 0.7f);
    }

    @Override
    protected float getShadowRadius(TurtleEntity turtleEntity) {
        float f = super.getShadowRadius(turtleEntity);
        if (turtleEntity.isBaby()) {
            return f * 0.83f;
        }
        return f;
    }

    @Override
    public Identifier getTexture(TurtleEntity turtleEntity) {
        return TEXTURE;
    }

    @Override
    protected /* synthetic */ float getShadowRadius(MobEntity mobEntity) {
        return this.getShadowRadius((TurtleEntity)mobEntity);
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntity livingEntity) {
        return this.getShadowRadius((TurtleEntity)livingEntity);
    }

    @Override
    public /* synthetic */ Identifier getTexture(Entity entity) {
        return this.getTexture((TurtleEntity)entity);
    }

    @Override
    protected /* synthetic */ float getShadowRadius(Entity entity) {
        return this.getShadowRadius((TurtleEntity)entity);
    }
}

