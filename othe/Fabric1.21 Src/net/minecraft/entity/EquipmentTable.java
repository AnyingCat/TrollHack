/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public record EquipmentTable(RegistryKey<LootTable> lootTable, Map<EquipmentSlot, Float> slotDropChances) {
    public static final Codec<Map<EquipmentSlot, Float>> SLOT_DROP_CHANCES_CODEC = Codec.either(Codec.FLOAT, Codec.unboundedMap(EquipmentSlot.CODEC, Codec.FLOAT)).xmap(either -> either.map(EquipmentTable::createSlotDropChances, Function.identity()), map -> {
        boolean bl = map.values().stream().distinct().count() == 1L;
        boolean bl2 = map.keySet().containsAll(Arrays.asList(EquipmentSlot.values()));
        if (bl && bl2) {
            return Either.left(map.values().stream().findFirst().orElse(Float.valueOf(0.0f)));
        }
        return Either.right(map);
    });
    public static final Codec<EquipmentTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).fieldOf("loot_table")).forGetter(EquipmentTable::lootTable), SLOT_DROP_CHANCES_CODEC.optionalFieldOf("slot_drop_chances", Map.of()).forGetter(EquipmentTable::slotDropChances)).apply((Applicative<EquipmentTable, ?>)instance, EquipmentTable::new));

    private static Map<EquipmentSlot, Float> createSlotDropChances(float dropChance) {
        return EquipmentTable.createSlotDropChances(List.of(EquipmentSlot.values()), dropChance);
    }

    private static Map<EquipmentSlot, Float> createSlotDropChances(List<EquipmentSlot> slots, float dropChance) {
        HashMap<EquipmentSlot, Float> map = Maps.newHashMap();
        for (EquipmentSlot equipmentSlot : slots) {
            map.put(equipmentSlot, Float.valueOf(dropChance));
        }
        return map;
    }
}

