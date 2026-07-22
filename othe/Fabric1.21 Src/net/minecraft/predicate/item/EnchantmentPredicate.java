/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Optional;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;

public record EnchantmentPredicate(Optional<RegistryEntryList<Enchantment>> enchantments, NumberRange.IntRange levels) {
    public static final Codec<EnchantmentPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryCodecs.entryList(RegistryKeys.ENCHANTMENT).optionalFieldOf("enchantments").forGetter(EnchantmentPredicate::enchantments), NumberRange.IntRange.CODEC.optionalFieldOf("levels", NumberRange.IntRange.ANY).forGetter(EnchantmentPredicate::levels)).apply((Applicative<EnchantmentPredicate, ?>)instance, EnchantmentPredicate::new));

    public EnchantmentPredicate(RegistryEntry<Enchantment> enchantment, NumberRange.IntRange levels) {
        this(Optional.of(RegistryEntryList.of(enchantment)), levels);
    }

    public EnchantmentPredicate(RegistryEntryList<Enchantment> enchantments, NumberRange.IntRange levels) {
        this(Optional.of(enchantments), levels);
    }

    public boolean test(ItemEnchantmentsComponent enchantmentsComponent) {
        if (this.enchantments.isPresent()) {
            for (RegistryEntry registryEntry : this.enchantments.get()) {
                if (!this.testLevel(enchantmentsComponent, registryEntry)) continue;
                return true;
            }
            return false;
        }
        if (this.levels != NumberRange.IntRange.ANY) {
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantmentsComponent.getEnchantmentEntries()) {
                if (!this.levels.test(entry.getIntValue())) continue;
                return true;
            }
            return false;
        }
        return !enchantmentsComponent.isEmpty();
    }

    private boolean testLevel(ItemEnchantmentsComponent enchantmentsComponent, RegistryEntry<Enchantment> enchantment) {
        int i = enchantmentsComponent.getLevel(enchantment);
        if (i == 0) {
            return false;
        }
        if (this.levels == NumberRange.IntRange.ANY) {
            return true;
        }
        return this.levels.test(i);
    }
}

