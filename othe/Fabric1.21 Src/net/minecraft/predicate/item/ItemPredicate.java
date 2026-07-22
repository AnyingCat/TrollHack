/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.item;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.ComponentPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.ItemSubPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

public record ItemPredicate(Optional<RegistryEntryList<Item>> items, NumberRange.IntRange count, ComponentPredicate components, Map<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates) implements Predicate<ItemStack>
{
    public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryCodecs.entryList(RegistryKeys.ITEM).optionalFieldOf("items").forGetter(ItemPredicate::items), NumberRange.IntRange.CODEC.optionalFieldOf("count", NumberRange.IntRange.ANY).forGetter(ItemPredicate::count), ComponentPredicate.CODEC.optionalFieldOf("components", ComponentPredicate.EMPTY).forGetter(ItemPredicate::components), ItemSubPredicate.PREDICATES_MAP_CODEC.optionalFieldOf("predicates", Map.of()).forGetter(ItemPredicate::subPredicates)).apply((Applicative<ItemPredicate, ?>)instance, ItemPredicate::new));

    @Override
    public boolean test(ItemStack stack) {
        if (this.items.isPresent() && !stack.isIn(this.items.get())) {
            return false;
        }
        if (!this.count.test(stack.getCount())) {
            return false;
        }
        if (!this.components.test(stack)) {
            return false;
        }
        for (ItemSubPredicate itemSubPredicate : this.subPredicates.values()) {
            if (itemSubPredicate.test(stack)) continue;
            return false;
        }
        return true;
    }

    @Override
    public /* synthetic */ boolean test(Object stack) {
        return this.test((ItemStack)stack);
    }

    public static class Builder {
        private Optional<RegistryEntryList<Item>> item = Optional.empty();
        private NumberRange.IntRange count = NumberRange.IntRange.ANY;
        private ComponentPredicate componentPredicate = ComponentPredicate.EMPTY;
        private final ImmutableMap.Builder<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates = ImmutableMap.builder();

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder items(ItemConvertible ... items) {
            this.item = Optional.of(RegistryEntryList.of(item -> item.asItem().getRegistryEntry(), items));
            return this;
        }

        public Builder tag(TagKey<Item> tag) {
            this.item = Optional.of(Registries.ITEM.getOrCreateEntryList(tag));
            return this;
        }

        public Builder count(NumberRange.IntRange count) {
            this.count = count;
            return this;
        }

        public <T extends ItemSubPredicate> Builder subPredicate(ItemSubPredicate.Type<T> type, T subPredicate) {
            this.subPredicates.put(type, subPredicate);
            return this;
        }

        public Builder component(ComponentPredicate componentPredicate) {
            this.componentPredicate = componentPredicate;
            return this;
        }

        public ItemPredicate build() {
            return new ItemPredicate(this.item, this.count, this.componentPredicate, this.subPredicates.build());
        }
    }
}

