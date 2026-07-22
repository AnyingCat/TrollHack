/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.network.PacketByteBuf;

public record AdvancementRequirements(List<List<String>> requirements) {
    public static final Codec<AdvancementRequirements> CODEC = Codec.STRING.listOf().listOf().xmap(AdvancementRequirements::new, AdvancementRequirements::requirements);
    public static final AdvancementRequirements EMPTY = new AdvancementRequirements(List.of());

    public AdvancementRequirements(PacketByteBuf buf) {
        this(buf.readList(bufx -> bufx.readList(PacketByteBuf::readString)));
    }

    public void writeRequirements(PacketByteBuf buf) {
        buf.writeCollection(this.requirements, (bufx, requirements) -> bufx.writeCollection(requirements, PacketByteBuf::writeString));
    }

    public static AdvancementRequirements allOf(Collection<String> requirements) {
        return new AdvancementRequirements(requirements.stream().map(List::of).toList());
    }

    public static AdvancementRequirements anyOf(Collection<String> requirements) {
        return new AdvancementRequirements(List.of(List.copyOf(requirements)));
    }

    public int getLength() {
        return this.requirements.size();
    }

    public boolean matches(Predicate<String> predicate) {
        if (this.requirements.isEmpty()) {
            return false;
        }
        for (List<String> list : this.requirements) {
            if (AdvancementRequirements.anyMatch(list, predicate)) continue;
            return false;
        }
        return true;
    }

    public int countMatches(Predicate<String> predicate) {
        int i = 0;
        for (List<String> list : this.requirements) {
            if (!AdvancementRequirements.anyMatch(list, predicate)) continue;
            ++i;
        }
        return i;
    }

    private static boolean anyMatch(List<String> requirements, Predicate<String> predicate) {
        for (String string : requirements) {
            if (!predicate.test(string)) continue;
            return true;
        }
        return false;
    }

    public DataResult<AdvancementRequirements> validate(Set<String> requirements) {
        ObjectOpenHashSet set = new ObjectOpenHashSet();
        for (List<String> list : this.requirements) {
            if (list.isEmpty() && requirements.isEmpty()) {
                return DataResult.error(() -> "Requirement entry cannot be empty");
            }
            set.addAll(list);
        }
        if (!requirements.equals(set)) {
            Sets.SetView<String> set2 = Sets.difference(requirements, set);
            Sets.SetView set3 = Sets.difference(set, requirements);
            return DataResult.error(() -> "Advancement completion requirements did not exactly match specified criteria. Missing: " + String.valueOf(set2) + ". Unknown: " + String.valueOf(set3));
        }
        return DataResult.success(this);
    }

    public boolean isEmpty() {
        return this.requirements.isEmpty();
    }

    @Override
    public String toString() {
        return this.requirements.toString();
    }

    public Set<String> getNames() {
        ObjectOpenHashSet<String> set = new ObjectOpenHashSet<String>();
        for (List<String> list : this.requirements) {
            set.addAll(list);
        }
        return set;
    }

    public static interface CriterionMerger {
        public static final CriterionMerger AND = AdvancementRequirements::allOf;
        public static final CriterionMerger OR = AdvancementRequirements::anyOf;

        public AdvancementRequirements create(Collection<String> var1);
    }
}

