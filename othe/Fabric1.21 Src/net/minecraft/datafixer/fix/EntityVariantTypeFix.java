/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.datafixer.fix.ChoiceFix;

public class EntityVariantTypeFix
extends ChoiceFix {
    private final String variantKey;
    private final IntFunction<String> variantIntToId;

    public EntityVariantTypeFix(Schema outputSchema, String name, DSL.TypeReference type, String entityId, String variantKey, IntFunction<String> variantIntToId) {
        super(outputSchema, false, name, type, entityId);
        this.variantKey = variantKey;
        this.variantIntToId = variantIntToId;
    }

    private static <T> Dynamic<T> updateEntity(Dynamic<T> entityDynamic, String oldVariantKey, String newVariantKey, Function<Dynamic<T>, Dynamic<T>> variantIntToId) {
        return entityDynamic.map(object3 -> {
            DynamicOps<Object> dynamicOps = entityDynamic.getOps();
            Function<Object, Object> function2 = object -> ((Dynamic)variantIntToId.apply(new Dynamic<Object>(dynamicOps, object))).getValue();
            return dynamicOps.get(object3, oldVariantKey).map(object2 -> dynamicOps.set(object3, newVariantKey, function2.apply(object2))).result().orElse(object3);
        });
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), entityDynamic -> EntityVariantTypeFix.updateEntity(entityDynamic, this.variantKey, "variant", variantDynamic -> DataFixUtils.orElse(variantDynamic.asNumber().map(variantInt -> variantDynamic.createString(this.variantIntToId.apply(variantInt.intValue()))).result(), variantDynamic)));
    }
}

