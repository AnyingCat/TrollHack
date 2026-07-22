/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.entry;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.CombinedEntry;
import net.minecraft.loot.entry.EntryCombiner;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;

public class AlternativeEntry
extends CombinedEntry {
    public static final MapCodec<AlternativeEntry> CODEC = AlternativeEntry.createCodec(AlternativeEntry::new);

    AlternativeEntry(List<LootPoolEntry> list, List<LootCondition> list2) {
        super(list, list2);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.ALTERNATIVES;
    }

    @Override
    protected EntryCombiner combine(List<? extends EntryCombiner> terms) {
        return switch (terms.size()) {
            case 0 -> ALWAYS_FALSE;
            case 1 -> terms.get(0);
            case 2 -> terms.get(0).or(terms.get(1));
            default -> (context, lootChoiceExpander) -> {
                for (EntryCombiner entryCombiner : terms) {
                    if (!entryCombiner.expand(context, lootChoiceExpander)) continue;
                    return true;
                }
                return false;
            };
        };
    }

    @Override
    public void validate(LootTableReporter reporter) {
        super.validate(reporter);
        for (int i = 0; i < this.children.size() - 1; ++i) {
            if (!((LootPoolEntry)this.children.get((int)i)).conditions.isEmpty()) continue;
            reporter.report("Unreachable entry!");
        }
    }

    public static Builder builder(LootPoolEntry.Builder<?> ... children) {
        return new Builder(children);
    }

    public static <E> Builder builder(Collection<E> children, Function<E, LootPoolEntry.Builder<?>> toBuilderFunction) {
        return new Builder((LootPoolEntry.Builder[])children.stream().map(toBuilderFunction::apply).toArray(LootPoolEntry.Builder[]::new));
    }

    public static class Builder
    extends LootPoolEntry.Builder<Builder> {
        private final ImmutableList.Builder<LootPoolEntry> children = ImmutableList.builder();

        public Builder(LootPoolEntry.Builder<?> ... children) {
            for (LootPoolEntry.Builder<?> builder : children) {
                this.children.add((Object)builder.build());
            }
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public Builder alternatively(LootPoolEntry.Builder<?> builder) {
            this.children.add((Object)builder.build());
            return this;
        }

        @Override
        public LootPoolEntry build() {
            return new AlternativeEntry((List<LootPoolEntry>)((Object)this.children.build()), this.getConditions());
        }

        @Override
        protected /* synthetic */ LootPoolEntry.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

