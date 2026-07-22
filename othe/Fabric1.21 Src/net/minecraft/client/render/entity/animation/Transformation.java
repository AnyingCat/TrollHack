/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public record Transformation(Target target, Keyframe[] keyframes) {

    @Environment(value=EnvType.CLIENT)
    public static interface Target {
        public void apply(ModelPart var1, Vector3f var2);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Interpolations {
        public static final Interpolation LINEAR = (dest, delta, keyframes, start, end, scale) -> {
            Vector3f vector3f = keyframes[start].target();
            Vector3f vector3f2 = keyframes[end].target();
            return vector3f.lerp(vector3f2, delta, dest).mul(scale);
        };
        public static final Interpolation CUBIC = (dest, delta, keyframes, start, end, scale) -> {
            Vector3f vector3f = keyframes[Math.max(0, start - 1)].target();
            Vector3f vector3f2 = keyframes[start].target();
            Vector3f vector3f3 = keyframes[end].target();
            Vector3f vector3f4 = keyframes[Math.min(keyframes.length - 1, end + 1)].target();
            dest.set(MathHelper.catmullRom(delta, vector3f.x(), vector3f2.x(), vector3f3.x(), vector3f4.x()) * scale, MathHelper.catmullRom(delta, vector3f.y(), vector3f2.y(), vector3f3.y(), vector3f4.y()) * scale, MathHelper.catmullRom(delta, vector3f.z(), vector3f2.z(), vector3f3.z(), vector3f4.z()) * scale);
            return dest;
        };
    }

    @Environment(value=EnvType.CLIENT)
    public static class Targets {
        public static final Target TRANSLATE = ModelPart::translate;
        public static final Target ROTATE = ModelPart::rotate;
        public static final Target SCALE = ModelPart::scale;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Interpolation {
        public Vector3f apply(Vector3f var1, float var2, Keyframe[] var3, int var4, int var5, float var6);
    }
}

