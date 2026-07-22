/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.google.common.collect.Iterables;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.collection.DefaultedList;

public final class ContainerComponent {
    private static final int ALL_SLOTS_EMPTY = -1;
    private static final int MAX_SLOTS = 256;
    public static final ContainerComponent DEFAULT = new ContainerComponent(DefaultedList.of());
    public static final Codec<ContainerComponent> CODEC = Slot.CODEC.sizeLimitedListOf(256).xmap(ContainerComponent::fromSlots, ContainerComponent::collectSlots);
    public static final PacketCodec<RegistryByteBuf, ContainerComponent> PACKET_CODEC = ItemStack.OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toList(256)).xmap(ContainerComponent::new, component -> component.stacks);
    private final DefaultedList<ItemStack> stacks;
    private final int hashCode;

    private ContainerComponent(DefaultedList<ItemStack> stacks) {
        if (stacks.size() > 256) {
            throw new IllegalArgumentException("Got " + stacks.size() + " items, but maximum is 256");
        }
        this.stacks = stacks;
        this.hashCode = ItemStack.listHashCode(stacks);
    }

    private ContainerComponent(int size) {
        this(DefaultedList.ofSize(size, ItemStack.EMPTY));
    }

    private ContainerComponent(List<ItemStack> stacks) {
        this(stacks.size());
        for (int i = 0; i < stacks.size(); ++i) {
            this.stacks.set(i, stacks.get(i));
        }
    }

    private static ContainerComponent fromSlots(List<Slot> slots) {
        OptionalInt optionalInt = slots.stream().mapToInt(Slot::index).max();
        if (optionalInt.isEmpty()) {
            return DEFAULT;
        }
        ContainerComponent containerComponent = new ContainerComponent(optionalInt.getAsInt() + 1);
        for (Slot slot : slots) {
            containerComponent.stacks.set(slot.index(), slot.item());
        }
        return containerComponent;
    }

    public static ContainerComponent fromStacks(List<ItemStack> stacks) {
        int i = ContainerComponent.findFirstNonEmptyIndex(stacks);
        if (i == -1) {
            return DEFAULT;
        }
        ContainerComponent containerComponent = new ContainerComponent(i + 1);
        for (int j = 0; j <= i; ++j) {
            containerComponent.stacks.set(j, stacks.get(j).copy());
        }
        return containerComponent;
    }

    private static int findFirstNonEmptyIndex(List<ItemStack> stacks) {
        for (int i = stacks.size() - 1; i >= 0; --i) {
            if (stacks.get(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private List<Slot> collectSlots() {
        ArrayList<Slot> list = new ArrayList<Slot>();
        for (int i = 0; i < this.stacks.size(); ++i) {
            ItemStack itemStack = this.stacks.get(i);
            if (itemStack.isEmpty()) continue;
            list.add(new Slot(i, itemStack));
        }
        return list;
    }

    public void copyTo(DefaultedList<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack itemStack = i < this.stacks.size() ? this.stacks.get(i) : ItemStack.EMPTY;
            stacks.set(i, itemStack.copy());
        }
    }

    public ItemStack copyFirstStack() {
        return this.stacks.isEmpty() ? ItemStack.EMPTY : this.stacks.get(0).copy();
    }

    public Stream<ItemStack> stream() {
        return this.stacks.stream().map(ItemStack::copy);
    }

    public Stream<ItemStack> streamNonEmpty() {
        return this.stacks.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy);
    }

    public Iterable<ItemStack> iterateNonEmpty() {
        return Iterables.filter(this.stacks, stack -> !stack.isEmpty());
    }

    public Iterable<ItemStack> iterateNonEmptyCopy() {
        return Iterables.transform(this.iterateNonEmpty(), ItemStack::copy);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContainerComponent)) return false;
        ContainerComponent containerComponent = (ContainerComponent)o;
        if (!ItemStack.stacksEqual(this.stacks, containerComponent.stacks)) return false;
        return true;
    }

    public int hashCode() {
        return this.hashCode;
    }

    record Slot(int index, ItemStack item) {
        public static final Codec<Slot> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(0, 255).fieldOf("slot")).forGetter(Slot::index), ((MapCodec)ItemStack.CODEC.fieldOf("item")).forGetter(Slot::item)).apply((Applicative<Slot, ?>)instance, Slot::new));
    }
}

