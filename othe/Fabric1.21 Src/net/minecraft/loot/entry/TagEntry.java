/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

public class TagEntry
extends LeafEntry {
    public static final MapCodec<TagEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)TagKey.unprefixedCodec(RegistryKeys.ITEM).fieldOf("name")).forGetter(entry -> entry.name), ((MapCodec)Codec.BOOL.fieldOf("expand")).forGetter(entry -> entry.expand)).and(TagEntry.addLeafFields(instance)).apply(instance, TagEntry::new));
    private final TagKey<Item> name;
    private final boolean expand;

    private TagEntry(TagKey<Item> name, boolean expand, int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) {
        super(weight, quality, conditions, functions);
        this.name = name;
        this.expand = expand;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.TAG;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        Registries.ITEM.iterateEntries(this.name).forEach(entry -> lootConsumer.accept(new ItemStack((RegistryEntry<Item>)entry)));
    }

    private boolean grow(LootContext context, Consumer<LootChoice> lootChoiceExpander) {
        if (this.test(context)) {
            for (final RegistryEntry<Item> registryEntry : Registries.ITEM.iterateEntries(this.name)) {
                lootChoiceExpander.accept(new LeafEntry.Choice(this){

                    @Override
                    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
                        lootConsumer.accept(new ItemStack(registryEntry));
                    }
                });
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean expand(LootContext lootContext, Consumer<LootChoice> consumer) {
        if (this.expand) {
            return this.grow(lootContext, consumer);
        }
        return super.expand(lootContext, consumer);
    }

    public static LeafEntry.Builder<?> builder(TagKey<Item> name) {
        return TagEntry.builder((int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) -> new TagEntry(name, false, weight, quality, conditions, functions));
    }

    public static LeafEntry.Builder<?> expandBuilder(TagKey<Item> name) {
        return TagEntry.builder((int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) -> new TagEntry(name, true, weight, quality, conditions, functions));
    }
}

