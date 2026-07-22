/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ItemEnchantmentsComponent
implements TooltipAppender {
    public static final ItemEnchantmentsComponent DEFAULT = new ItemEnchantmentsComponent(new Object2IntOpenHashMap<RegistryEntry<Enchantment>>(), true);
    private static final Codec<Integer> ENCHANTMENT_LEVEL_CODEC = Codec.intRange(0, 255);
    private static final Codec<Object2IntOpenHashMap<RegistryEntry<Enchantment>>> INLINE_CODEC = Codec.unboundedMap(Enchantment.ENTRY_CODEC, ENCHANTMENT_LEVEL_CODEC).xmap(Object2IntOpenHashMap::new, Function.identity());
    private static final Codec<ItemEnchantmentsComponent> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)INLINE_CODEC.fieldOf("levels")).forGetter(component -> component.enchantments), Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(component -> component.showInTooltip)).apply((Applicative<ItemEnchantmentsComponent, ?>)instance, ItemEnchantmentsComponent::new));
    public static final Codec<ItemEnchantmentsComponent> CODEC = Codec.withAlternative(BASE_CODEC, INLINE_CODEC, map -> new ItemEnchantmentsComponent((Object2IntOpenHashMap<RegistryEntry<Enchantment>>)map, true));
    public static final PacketCodec<RegistryByteBuf, ItemEnchantmentsComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.map(Object2IntOpenHashMap::new, Enchantment.ENTRY_PACKET_CODEC, PacketCodecs.VAR_INT), component -> component.enchantments, PacketCodecs.BOOL, component -> component.showInTooltip, ItemEnchantmentsComponent::new);
    final Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchantments;
    final boolean showInTooltip;

    ItemEnchantmentsComponent(Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchantments, boolean showInTooltip) {
        this.enchantments = enchantments;
        this.showInTooltip = showInTooltip;
        for (Object2IntMap.Entry entry : enchantments.object2IntEntrySet()) {
            int i = entry.getIntValue();
            if (i >= 0 && i <= 255) continue;
            throw new IllegalArgumentException("Enchantment " + String.valueOf(entry.getKey()) + " has invalid level " + i);
        }
    }

    public int getLevel(RegistryEntry<Enchantment> enchantment) {
        return this.enchantments.getInt(enchantment);
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        if (!this.showInTooltip) {
            return;
        }
        RegistryWrapper.WrapperLookup wrapperLookup = context.getRegistryLookup();
        RegistryEntryList<Enchantment> registryEntryList = ItemEnchantmentsComponent.getTooltipOrderList(wrapperLookup, RegistryKeys.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);
        for (RegistryEntry registryEntry : registryEntryList) {
            int i = this.enchantments.getInt(registryEntry);
            if (i <= 0) continue;
            tooltip.accept(Enchantment.getName(registryEntry, i));
        }
        for (Object2IntMap.Entry entry : this.enchantments.object2IntEntrySet()) {
            RegistryEntry registryEntry2 = (RegistryEntry)entry.getKey();
            if (registryEntryList.contains(registryEntry2)) continue;
            tooltip.accept(Enchantment.getName((RegistryEntry)entry.getKey(), entry.getIntValue()));
        }
    }

    private static <T> RegistryEntryList<T> getTooltipOrderList(@Nullable RegistryWrapper.WrapperLookup registryLookup, RegistryKey<Registry<T>> registryRef, TagKey<T> tooltipOrderTag) {
        Optional<RegistryEntryList.Named<T>> optional;
        if (registryLookup != null && (optional = registryLookup.getWrapperOrThrow(registryRef).getOptional(tooltipOrderTag)).isPresent()) {
            return optional.get();
        }
        return RegistryEntryList.of(new RegistryEntry[0]);
    }

    public ItemEnchantmentsComponent withShowInTooltip(boolean showInTooltip) {
        return new ItemEnchantmentsComponent(this.enchantments, showInTooltip);
    }

    public Set<RegistryEntry<Enchantment>> getEnchantments() {
        return Collections.unmodifiableSet(this.enchantments.keySet());
    }

    public Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> getEnchantmentEntries() {
        return Collections.unmodifiableSet(this.enchantments.object2IntEntrySet());
    }

    public int getSize() {
        return this.enchantments.size();
    }

    public boolean isEmpty() {
        return this.enchantments.isEmpty();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ItemEnchantmentsComponent) {
            ItemEnchantmentsComponent itemEnchantmentsComponent = (ItemEnchantmentsComponent)o;
            return this.showInTooltip == itemEnchantmentsComponent.showInTooltip && this.enchantments.equals(itemEnchantmentsComponent.enchantments);
        }
        return false;
    }

    public int hashCode() {
        int i = this.enchantments.hashCode();
        i = 31 * i + (this.showInTooltip ? 1 : 0);
        return i;
    }

    public String toString() {
        return "ItemEnchantments{enchantments=" + String.valueOf(this.enchantments) + ", showInTooltip=" + this.showInTooltip + "}";
    }

    public static class Builder {
        private final Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchantments = new Object2IntOpenHashMap();
        private final boolean showInTooltip;

        public Builder(ItemEnchantmentsComponent enchantmentsComponent) {
            this.enchantments.putAll((Map<RegistryEntry<Enchantment>, Integer>)enchantmentsComponent.enchantments);
            this.showInTooltip = enchantmentsComponent.showInTooltip;
        }

        public void set(RegistryEntry<Enchantment> enchantment, int level) {
            if (level <= 0) {
                this.enchantments.removeInt(enchantment);
            } else {
                this.enchantments.put(enchantment, Math.min(level, 255));
            }
        }

        public void add(RegistryEntry<Enchantment> enchantment, int level) {
            if (level > 0) {
                this.enchantments.merge(enchantment, Math.min(level, 255), Integer::max);
            }
        }

        public void remove(Predicate<RegistryEntry<Enchantment>> predicate) {
            this.enchantments.keySet().removeIf(predicate);
        }

        public int getLevel(RegistryEntry<Enchantment> enchantment) {
            return this.enchantments.getOrDefault((Object)enchantment, 0);
        }

        public Set<RegistryEntry<Enchantment>> getEnchantments() {
            return this.enchantments.keySet();
        }

        public ItemEnchantmentsComponent build() {
            return new ItemEnchantmentsComponent(this.enchantments, this.showInTooltip);
        }
    }
}

