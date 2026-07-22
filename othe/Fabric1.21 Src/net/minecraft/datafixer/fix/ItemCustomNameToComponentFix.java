/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.TextFixes;

public class ItemCustomNameToComponentFix
extends DataFix {
    public ItemCustomNameToComponentFix(Schema outputSchema, boolean changesTyped) {
        super(outputSchema, changesTyped);
    }

    private Dynamic<?> fixCustomName(Dynamic<?> tagDynamic) {
        Optional<Dynamic<?>> optional = tagDynamic.get("display").result();
        if (optional.isPresent()) {
            Dynamic dynamic = optional.get();
            Optional<String> optional2 = dynamic.get("Name").asString().result();
            if (optional2.isPresent()) {
                dynamic = dynamic.set("Name", TextFixes.text(dynamic.getOps(), optional2.get()));
            }
            return tagDynamic.set("display", dynamic);
        }
        return tagDynamic;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder<?> opticFinder = type.findField("tag");
        return this.fixTypeEverywhereTyped("ItemCustomNameToComponentFix", type, itemStackTyped -> itemStackTyped.updateTyped(opticFinder, tagTyped -> tagTyped.update(DSL.remainderFinder(), this::fixCustomName)));
    }
}

