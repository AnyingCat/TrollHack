/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

/**
 * A set of button textures. It contains four texture choices, one of each of the cases
 * where a button is enabled/disabled and focused/not focused.
 * 
 *  * @param enabled the texture for when the widget is enabled, but not focused
 * @param disabled the texture for when the widget is disabled, but not focused
 * @param enabledFocused the texture for when the widget is enabled and focused
 * @param disabledFocused the texture for when the widget is disabled and focused
 */
@Environment(value=EnvType.CLIENT)
public record ButtonTextures(Identifier enabled, Identifier disabled, Identifier enabledFocused, Identifier disabledFocused) {
    /**
     * Constructs a set of button textures where only focusing the widget affects
     * the textures.
     * 
     * @param unfocused the texture for when the widget is not focused
     * @param focused the texture for when the widget is focused
     */
    public ButtonTextures(Identifier unfocused, Identifier focused) {
        this(unfocused, unfocused, focused, focused);
    }

    /**
     * Constructs a set of button textures where both disabled cases use the same texture.
     * 
     * @param focused the texture for when the widget is enabled and focused
     * @param disabled the texture for when the widget is disabled
     * @param enabled the texture for when the widget is enabled, but not focused
     */
    public ButtonTextures(Identifier enabled, Identifier disabled, Identifier focused) {
        this(enabled, disabled, focused, disabled);
    }

    /**
     * Gets a specific texture option from this texture set.
     * 
     * @return the texture identifier matching the widget state
     * 
     * @param enabled {@code true} if the widget is enabled, {@code false} otherwise
     * @param focused {@code true} if the widget is focused, {@code false} otherwise
     */
    public Identifier get(boolean enabled, boolean focused) {
        if (enabled) {
            return focused ? this.enabledFocused : this.enabled;
        }
        return focused ? this.disabledFocused : this.disabled;
    }
}

