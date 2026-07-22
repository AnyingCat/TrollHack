/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceWriteReadFix;

public class TippedArrowPotionToItemFix
extends ChoiceWriteReadFix {
    public TippedArrowPotionToItemFix(Schema outputSchema) {
        super(outputSchema, false, "TippedArrowPotionToItemFix", TypeReferences.ENTITY, "minecraft:arrow");
    }

    @Override
    protected <T> Dynamic<T> transform(Dynamic<T> data) {
        Optional optional = data.get("Potion").result();
        Optional optional2 = data.get("custom_potion_effects").result();
        Optional optional3 = data.get("Color").result();
        if (optional.isEmpty() && optional2.isEmpty() && optional3.isEmpty()) {
            return data;
        }
        return data.remove("Potion").remove("custom_potion_effects").remove("Color").update("item", itemDynamic -> {
            Dynamic dynamic = itemDynamic.get("tag").orElseEmptyMap();
            if (optional.isPresent()) {
                dynamic = dynamic.set("Potion", (Dynamic)optional.get());
            }
            if (optional2.isPresent()) {
                dynamic = dynamic.set("custom_potion_effects", (Dynamic)optional2.get());
            }
            if (optional3.isPresent()) {
                dynamic = dynamic.set("CustomPotionColor", (Dynamic)optional3.get());
            }
            return itemDynamic.set("tag", dynamic);
        });
    }
}

