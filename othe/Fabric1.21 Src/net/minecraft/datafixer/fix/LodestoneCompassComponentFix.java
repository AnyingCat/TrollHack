/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.fix.ComponentFix;

public class LodestoneCompassComponentFix
extends ComponentFix {
    public LodestoneCompassComponentFix(Schema outputSchema) {
        super(outputSchema, "LodestoneCompassComponentFix", "minecraft:lodestone_target", "minecraft:lodestone_tracker");
    }

    @Override
    protected <T> Dynamic<T> fixComponent(Dynamic<T> dynamic) {
        Optional<Dynamic<T>> optional = dynamic.get("pos").result();
        Optional<Dynamic<T>> optional2 = dynamic.get("dimension").result();
        dynamic = dynamic.remove("pos").remove("dimension");
        if (optional.isPresent() && optional2.isPresent()) {
            dynamic = dynamic.set("target", dynamic.emptyMap().set("pos", optional.get()).set("dimension", optional2.get()));
        }
        return dynamic;
    }
}

