/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import net.minecraft.util.ActionResult;

public enum ItemActionResult {
    SUCCESS,
    CONSUME,
    CONSUME_PARTIAL,
    PASS_TO_DEFAULT_BLOCK_INTERACTION,
    SKIP_DEFAULT_BLOCK_INTERACTION,
    FAIL;


    public boolean isAccepted() {
        return this.toActionResult().isAccepted();
    }

    public static ItemActionResult success(boolean swingHand) {
        return swingHand ? SUCCESS : CONSUME;
    }

    public ActionResult toActionResult() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> ActionResult.SUCCESS;
            case 1 -> ActionResult.CONSUME;
            case 2 -> ActionResult.CONSUME_PARTIAL;
            case 3, 4 -> ActionResult.PASS;
            case 5 -> ActionResult.FAIL;
        };
    }
}

