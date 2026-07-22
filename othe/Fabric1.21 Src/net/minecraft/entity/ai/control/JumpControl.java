/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.control;

import net.minecraft.entity.ai.control.Control;
import net.minecraft.entity.mob.MobEntity;

public class JumpControl
implements Control {
    private final MobEntity entity;
    protected boolean active;

    public JumpControl(MobEntity entity) {
        this.entity = entity;
    }

    public void setActive() {
        this.active = true;
    }

    public void tick() {
        this.entity.setJumping(this.active);
        this.active = false;
    }
}

