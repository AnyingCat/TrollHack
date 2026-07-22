/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.ComponentPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.network.ServerPlayerEntity;

public class InventoryChangedCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, PlayerInventory inventory, ItemStack stack) {
        int i = 0;
        int j = 0;
        int k = 0;
        for (int l = 0; l < inventory.size(); ++l) {
            ItemStack itemStack = inventory.getStack(l);
            if (itemStack.isEmpty()) {
                ++j;
                continue;
            }
            ++k;
            if (itemStack.getCount() < itemStack.getMaxCount()) continue;
            ++i;
        }
        this.trigger(player, inventory, stack, i, j, k);
    }

    private void trigger(ServerPlayerEntity player, PlayerInventory inventory, ItemStack stack, int full, int empty, int occupied) {
        this.trigger(player, conditions -> conditions.matches(inventory, stack, full, empty, occupied));
    }

    public record Conditions(Optional<LootContextPredicate> player, Slots slots, List<ItemPredicate> items) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), Slots.CODEC.optionalFieldOf("slots", Slots.ANY).forGetter(Conditions::slots), ItemPredicate.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(Conditions::items)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> items(ItemPredicate.Builder ... items) {
            return Conditions.items((ItemPredicate[])Stream.of(items).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
        }

        public static AdvancementCriterion<Conditions> items(ItemPredicate ... items) {
            return Criteria.INVENTORY_CHANGED.create(new Conditions(Optional.empty(), Slots.ANY, List.of(items)));
        }

        public static AdvancementCriterion<Conditions> items(ItemConvertible ... items) {
            ItemPredicate[] itemPredicates = new ItemPredicate[items.length];
            for (int i = 0; i < items.length; ++i) {
                itemPredicates[i] = new ItemPredicate(Optional.of(RegistryEntryList.of(items[i].asItem().getRegistryEntry())), NumberRange.IntRange.ANY, ComponentPredicate.EMPTY, Map.of());
            }
            return Conditions.items(itemPredicates);
        }

        public boolean matches(PlayerInventory inventory, ItemStack stack, int full, int empty, int occupied) {
            if (!this.slots.test(full, empty, occupied)) {
                return false;
            }
            if (this.items.isEmpty()) {
                return true;
            }
            if (this.items.size() == 1) {
                return !stack.isEmpty() && this.items.get(0).test(stack);
            }
            ObjectArrayList<ItemPredicate> list = new ObjectArrayList<ItemPredicate>(this.items);
            int i = inventory.size();
            for (int j = 0; j < i; ++j) {
                if (list.isEmpty()) {
                    return true;
                }
                ItemStack itemStack = inventory.getStack(j);
                if (itemStack.isEmpty()) continue;
                list.removeIf(item -> item.test(itemStack));
            }
            return list.isEmpty();
        }

        public record Slots(NumberRange.IntRange occupied, NumberRange.IntRange full, NumberRange.IntRange empty) {
            public static final Codec<Slots> CODEC = RecordCodecBuilder.create(instance -> instance.group(NumberRange.IntRange.CODEC.optionalFieldOf("occupied", NumberRange.IntRange.ANY).forGetter(Slots::occupied), NumberRange.IntRange.CODEC.optionalFieldOf("full", NumberRange.IntRange.ANY).forGetter(Slots::full), NumberRange.IntRange.CODEC.optionalFieldOf("empty", NumberRange.IntRange.ANY).forGetter(Slots::empty)).apply((Applicative<Slots, ?>)instance, Slots::new));
            public static final Slots ANY = new Slots(NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, NumberRange.IntRange.ANY);

            public boolean test(int full, int empty, int occupied) {
                if (!this.full.test(full)) {
                    return false;
                }
                if (!this.empty.test(empty)) {
                    return false;
                }
                return this.occupied.test(occupied);
            }
        }
    }
}

