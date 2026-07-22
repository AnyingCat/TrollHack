/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.entry;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.CombinedEntry;
import net.minecraft.loot.entry.EntryCombiner;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;

public class SequenceEntry
extends CombinedEntry {
    public static final MapCodec<SequenceEntry> CODEC = SequenceEntry.createCodec(SequenceEntry::new);

    SequenceEntry(List<LootPoolEntry> list, List<LootCondition> list2) {
        super(list, list2);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.SEQUENCE;
    }

    @Override
    protected EntryCombiner combine(List<? extends EntryCombiner> terms) {
        return switch (terms.size()) {
            case 0 -> ALWAYS_TRUE;
            case 1 -> terms.get(0);
            case 2 -> terms.get(0).and(terms.get(1));
            default -> (context, lootChoiceExpander) -> {
                for (EntryCombiner entryCombiner : terms) {
                    if (entryCombiner.expand(context, lootChoiceExpander)) continue;
                    return false;
                }
                return true;
            };
        };
    }

    public static Builder create(LootPoolEntry.Builder<?> ... entries) {
        return new Builder(entries);
    }

    public static class Builder
    extends LootPoolEntry.Builder<Builder> {
        private final ImmutableList.Builder<LootPoolEntry> entries = ImmutableList.builder();

        public Builder(LootPoolEntry.Builder<?> ... entries) {
            for (LootPoolEntry.Builder<?> builder : entries) {
                this.entries.add((Object)builder.build());
            }
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public Builder groupEntry(LootPoolEntry.Builder<?> entry) {
            this.entries.add((Object)entry.build());
            return this;
        }

        @Override
        public LootPoolEntry build() {
            return new SequenceEntry((List<LootPoolEntry>)((Object)this.entries.build()), this.getConditions());
        }

        @Override
        protected /* synthetic */ LootPoolEntry.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

