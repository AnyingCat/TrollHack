/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class StriderGravityFix
extends ChoiceFix {
    public StriderGravityFix(Schema outputschema, boolean changesType) {
        super(outputschema, changesType, "StriderGravityFix", TypeReferences.ENTITY, "minecraft:strider");
    }

    public Dynamic<?> updateNoGravityNbt(Dynamic<?> striderDynamic) {
        if (striderDynamic.get("NoGravity").asBoolean(false)) {
            return striderDynamic.set("NoGravity", striderDynamic.createBoolean(false));
        }
        return striderDynamic;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::updateNoGravityNbt);
    }
}

