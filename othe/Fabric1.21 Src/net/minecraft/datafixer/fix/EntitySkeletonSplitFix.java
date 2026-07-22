/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.datafixer.fix.EntitySimpleTransformFix;

public class EntitySkeletonSplitFix
extends EntitySimpleTransformFix {
    public EntitySkeletonSplitFix(Schema outputSchema, boolean changesType) {
        super("EntitySkeletonSplitFix", outputSchema, changesType);
    }

    @Override
    protected Pair<String, Dynamic<?>> transform(String choice, Dynamic<?> entityDynamic) {
        if (Objects.equals(choice, "Skeleton")) {
            int i = entityDynamic.get("SkeletonType").asInt(0);
            if (i == 1) {
                choice = "WitherSkeleton";
            } else if (i == 2) {
                choice = "Stray";
            }
        }
        return Pair.of(choice, entityDynamic);
    }
}

