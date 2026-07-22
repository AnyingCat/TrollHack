/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceWriteReadFix;

public class PrimedTntBlockStateFix
extends ChoiceWriteReadFix {
    public PrimedTntBlockStateFix(Schema outputSchema) {
        super(outputSchema, true, "PrimedTnt BlockState fixer", TypeReferences.ENTITY, "minecraft:tnt");
    }

    private static <T> Dynamic<T> fixFuse(Dynamic<T> data) {
        Optional<Dynamic<T>> optional = data.get("Fuse").get().result();
        if (optional.isPresent()) {
            return data.set("fuse", optional.get());
        }
        return data;
    }

    private static <T> Dynamic<T> fixBlockState(Dynamic<T> data) {
        return data.set("block_state", data.createMap(Map.of(data.createString("Name"), data.createString("minecraft:tnt"))));
    }

    @Override
    protected <T> Dynamic<T> transform(Dynamic<T> data) {
        return PrimedTntBlockStateFix.fixFuse(PrimedTntBlockStateFix.fixBlockState(data));
    }
}

