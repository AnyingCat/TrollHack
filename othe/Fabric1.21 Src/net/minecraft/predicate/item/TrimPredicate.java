/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;

public record TrimPredicate(Optional<RegistryEntryList<ArmorTrimMaterial>> material, Optional<RegistryEntryList<ArmorTrimPattern>> pattern) implements ComponentSubPredicate<ArmorTrim>
{
    public static final Codec<TrimPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryCodecs.entryList(RegistryKeys.TRIM_MATERIAL).optionalFieldOf("material").forGetter(TrimPredicate::material), RegistryCodecs.entryList(RegistryKeys.TRIM_PATTERN).optionalFieldOf("pattern").forGetter(TrimPredicate::pattern)).apply((Applicative<TrimPredicate, ?>)instance, TrimPredicate::new));

    @Override
    public ComponentType<ArmorTrim> getComponentType() {
        return DataComponentTypes.TRIM;
    }

    @Override
    public boolean test(ItemStack itemStack, ArmorTrim armorTrim) {
        if (this.material.isPresent() && !this.material.get().contains(armorTrim.getMaterial())) {
            return false;
        }
        return !this.pattern.isPresent() || this.pattern.get().contains(armorTrim.getPattern());
    }
}

