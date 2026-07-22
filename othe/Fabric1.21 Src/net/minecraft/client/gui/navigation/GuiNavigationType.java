/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.navigation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * An enum representing the type of navigation input method.
 */
@Environment(value=EnvType.CLIENT)
public enum GuiNavigationType {
    NONE,
    MOUSE,
    KEYBOARD_ARROW,
    KEYBOARD_TAB;


    public boolean isMouse() {
        return this == MOUSE;
    }

    public boolean isKeyboard() {
        return this == KEYBOARD_ARROW || this == KEYBOARD_TAB;
    }
}

