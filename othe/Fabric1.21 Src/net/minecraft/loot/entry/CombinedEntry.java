/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.entry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.EntryCombiner;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryTypes;

public abstract class CombinedEntry
extends LootPoolEntry {
    protected final List<LootPoolEntry> children;
    private final EntryCombiner predicate;

    protected CombinedEntry(List<LootPoolEntry> terms, List<LootCondition> conditions) {
        super(conditions);
        this.children = terms;
        this.predicate = this.combine(terms);
    }

    @Override
    public void validate(LootTableReporter reporter) {
        super.validate(reporter);
        if (this.children.isEmpty()) {
            reporter.report("Empty children list");
        }
        for (int i = 0; i < this.children.size(); ++i) {
            this.children.get(i).validate(reporter.makeChild(".entry[" + i + "]"));
        }
    }

    protected abstract EntryCombiner combine(List<? extends EntryCombiner> var1);

    @Override
    public final boolean expand(LootContext lootContext, Consumer<LootChoice> consumer) {
        if (!this.test(lootContext)) {
            return false;
        }
        return this.predicate.expand(lootContext, consumer);
    }

    public static <T extends CombinedEntry> MapCodec<T> createCodec(Factory<T> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(LootPoolEntryTypes.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter(entry -> entry.children)).and(CombinedEntry.addConditionsField(instance).t1()).apply(instance, factory::create));
    }

    @FunctionalInterface
    public static interface Factory<T extends CombinedEntry> {
        public T create(List<LootPoolEntry> var1, List<LootCondition> var2);
    }
}

