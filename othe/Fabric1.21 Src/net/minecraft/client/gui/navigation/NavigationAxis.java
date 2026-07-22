/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.navigation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.NavigationDirection;

@Environment(value=EnvType.CLIENT)
public enum NavigationAxis {
    HORIZONTAL,
    VERTICAL;


    public NavigationAxis getOther() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> VERTICAL;
            case 1 -> HORIZONTAL;
        };
    }

    public NavigationDirection getPositiveDirection() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> NavigationDirection.RIGHT;
            case 1 -> NavigationDirection.DOWN;
        };
    }

    public NavigationDirection getNegativeDirection() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> NavigationDirection.LEFT;
            case 1 -> NavigationDirection.UP;
        };
    }

    public NavigationDirection getDirection(boolean positive) {
        return positive ? this.getPositiveDirection() : this.getNegativeDirection();
    }
}

