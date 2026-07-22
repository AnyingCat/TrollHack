/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.inventory;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.SlotRange;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class SlotRanges {
    private static final List<SlotRange> SLOT_RANGES = Util.make(new ArrayList(), list -> {
        SlotRanges.createAndAdd((List<SlotRange>)list, "contents", 0);
        SlotRanges.createAndAdd((List<SlotRange>)list, "container.", 0, 54);
        SlotRanges.createAndAdd((List<SlotRange>)list, "hotbar.", 0, 9);
        SlotRanges.createAndAdd((List<SlotRange>)list, "inventory.", 9, 27);
        SlotRanges.createAndAdd((List<SlotRange>)list, "enderchest.", 200, 27);
        SlotRanges.createAndAdd((List<SlotRange>)list, "villager.", 300, 8);
        SlotRanges.createAndAdd((List<SlotRange>)list, "horse.", 500, 15);
        int i = EquipmentSlot.MAINHAND.getOffsetEntitySlotId(98);
        int j = EquipmentSlot.OFFHAND.getOffsetEntitySlotId(98);
        SlotRanges.createAndAdd((List<SlotRange>)list, "weapon", i);
        SlotRanges.createAndAdd((List<SlotRange>)list, "weapon.mainhand", i);
        SlotRanges.createAndAdd((List<SlotRange>)list, "weapon.offhand", j);
        SlotRanges.createAndAdd((List<SlotRange>)list, "weapon.*", new int[]{i, j});
        i = EquipmentSlot.HEAD.getOffsetEntitySlotId(100);
        j = EquipmentSlot.CHEST.getOffsetEntitySlotId(100);
        int k = EquipmentSlot.LEGS.getOffsetEntitySlotId(100);
        int l = EquipmentSlot.FEET.getOffsetEntitySlotId(100);
        int m = EquipmentSlot.BODY.getOffsetEntitySlotId(105);
        SlotRanges.createAndAdd((List<SlotRange>)list, "armor.head", i);
        SlotRanges.createAndAdd((List<SlotRange>)list, "armor.chest", j);
        SlotRanges.createAndAdd((List<SlotRange>)list, "armor.legs", k);
        SlotRanges.createAndAdd((List<SlotRange>)list, "armor.feet", l);
        SlotRanges.createAndAdd((List<SlotRange>)list, "armor.body", m);
        SlotRanges.createAndAdd((List<SlotRange>)list, "armor.*", i, j, k, l, m);
        SlotRanges.createAndAdd((List<SlotRange>)list, "horse.saddle", 400);
        SlotRanges.createAndAdd((List<SlotRange>)list, "horse.chest", 499);
        SlotRanges.createAndAdd((List<SlotRange>)list, "player.cursor", 499);
        SlotRanges.createAndAdd((List<SlotRange>)list, "player.crafting.", 500, 4);
    });
    public static final Codec<SlotRange> CODEC = StringIdentifiable.createBasicCodec(() -> SLOT_RANGES.toArray(new SlotRange[0]));
    private static final Function<String, SlotRange> FROM_NAME = StringIdentifiable.createMapper((StringIdentifiable[])SLOT_RANGES.toArray(new SlotRange[0]), name -> name);

    private static SlotRange create(String name, int slotId) {
        return SlotRange.create(name, IntLists.singleton(slotId));
    }

    private static SlotRange create(String name, IntList slotIds) {
        return SlotRange.create(name, IntLists.unmodifiable(slotIds));
    }

    private static SlotRange create(String name, int ... slotIds) {
        return SlotRange.create(name, IntList.of(slotIds));
    }

    private static void createAndAdd(List<SlotRange> list, String name, int slotId) {
        list.add(SlotRanges.create(name, slotId));
    }

    private static void createAndAdd(List<SlotRange> list, String baseName, int firstSlotId, int lastSlotId) {
        IntArrayList intList = new IntArrayList(lastSlotId);
        for (int i = 0; i < lastSlotId; ++i) {
            int j = firstSlotId + i;
            list.add(SlotRanges.create(baseName + i, j));
            intList.add(j);
        }
        list.add(SlotRanges.create(baseName + "*", intList));
    }

    private static void createAndAdd(List<SlotRange> list, String name, int ... slots) {
        list.add(SlotRanges.create(name, slots));
    }

    @Nullable
    public static SlotRange fromName(String name) {
        return FROM_NAME.apply(name);
    }

    public static Stream<String> streamNames() {
        return SLOT_RANGES.stream().map(StringIdentifiable::asString);
    }

    public static Stream<String> streamSingleSlotNames() {
        return SLOT_RANGES.stream().filter(slotRange -> slotRange.getSlotCount() == 1).map(StringIdentifiable::asString);
    }
}

