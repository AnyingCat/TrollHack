/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.animation.Transformation;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public record Keyframe(float timestamp, Vector3f target, Transformation.Interpolation interpolation) {
}

