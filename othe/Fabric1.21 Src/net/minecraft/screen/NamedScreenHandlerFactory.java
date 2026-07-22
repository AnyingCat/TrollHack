/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import net.fabricmc.fabric.api.screenhandler.v1.FabricScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.text.Text;

/**
 * A screen handler factory with a name (title). This is passed to {@link
 * net.minecraft.entity.player.PlayerEntity#openHandledScreen} to open a screen
 * handler.
 * 
 * <p>In vanilla, most block entity instances implement this interface, allowing them to be used
 * as a factory. {@link SimpleNamedScreenHandlerFactory} is a screen handler factory
 * implementation for use cases that do not involve a block entity.
 */
public interface NamedScreenHandlerFactory
extends ScreenHandlerFactory,
FabricScreenHandlerFactory {
    /**
     * Returns the title of this screen handler; will be a part of the open
     * screen packet sent to the client.
     */
    public Text getDisplayName();
}

