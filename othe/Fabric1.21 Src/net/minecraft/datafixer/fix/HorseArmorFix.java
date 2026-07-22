/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.Streams;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceWriteReadFix;

public class HorseArmorFix
extends ChoiceWriteReadFix {
    private final String oldNbtKey;
    private final boolean removeOldArmor;

    public HorseArmorFix(Schema outputSchema, String entityId, String oldNbtKey, boolean removeOldArmor) {
        super(outputSchema, true, "Horse armor fix for " + entityId, TypeReferences.ENTITY, entityId);
        this.oldNbtKey = oldNbtKey;
        this.removeOldArmor = removeOldArmor;
    }

    @Override
    protected <T> Dynamic<T> transform(Dynamic<T> data) {
        Optional<Dynamic<T>> optional = data.get(this.oldNbtKey).result();
        if (optional.isPresent()) {
            Dynamic<T> dynamic = optional.get();
            Dynamic<T> dynamic2 = data.remove(this.oldNbtKey);
            if (this.removeOldArmor) {
                dynamic2 = dynamic2.update("ArmorItems", armorItemsDynamic -> armorItemsDynamic.createList(Streams.mapWithIndex(armorItemsDynamic.asStream(), (itemDynamic, slot) -> slot == 2L ? itemDynamic.emptyMap() : itemDynamic)));
                dynamic2 = dynamic2.update("ArmorDropChances", armorDropChancesDynamic -> armorDropChancesDynamic.createList(Streams.mapWithIndex(armorDropChancesDynamic.asStream(), (dropChanceDynamic, slot) -> slot == 2L ? dropChanceDynamic.createFloat(0.085f) : dropChanceDynamic)));
            }
            dynamic2 = dynamic2.set("body_armor_item", dynamic);
            dynamic2 = dynamic2.set("body_armor_drop_chance", data.createFloat(2.0f));
            return dynamic2;
        }
        return data;
    }
}

