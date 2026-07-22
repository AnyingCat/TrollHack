/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class HangingSignEditScreen
extends AbstractSignEditScreen {
    public static final float BACKGROUND_SCALE = 4.5f;
    private static final Vector3f TEXT_SCALE = new Vector3f(1.0f, 1.0f, 1.0f);
    private static final int field_40433 = 16;
    private static final int field_40434 = 16;
    private final Identifier texture;

    public HangingSignEditScreen(SignBlockEntity signBlockEntity, boolean bl, boolean bl2) {
        super(signBlockEntity, bl, bl2, Text.translatable("hanging_sign.edit"));
        this.texture = Identifier.ofVanilla("textures/gui/hanging_signs/" + this.signType.name() + ".png");
    }

    @Override
    protected void translateForRender(DrawContext context, BlockState state) {
        context.getMatrices().translate((float)this.width / 2.0f, 125.0f, 50.0f);
    }

    @Override
    protected void renderSignBackground(DrawContext context, BlockState state) {
        context.getMatrices().translate(0.0f, -13.0f, 0.0f);
        context.getMatrices().scale(4.5f, 4.5f, 1.0f);
        context.drawTexture(this.texture, -8, -8, 0.0f, 0.0f, 16, 16, 16, 16);
    }

    @Override
    protected Vector3f getTextScale() {
        return TEXT_SCALE;
    }
}

