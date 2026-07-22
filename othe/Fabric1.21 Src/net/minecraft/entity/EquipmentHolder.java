/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentTable;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

public interface EquipmentHolder {
    public void equipStack(EquipmentSlot var1, ItemStack var2);

    public ItemStack getEquippedStack(EquipmentSlot var1);

    public void setEquipmentDropChance(EquipmentSlot var1, float var2);

    default public void setEquipmentFromTable(EquipmentTable equipmentTable, LootContextParameterSet parameters) {
        this.setEquipmentFromTable(equipmentTable.lootTable(), parameters, equipmentTable.slotDropChances());
    }

    default public void setEquipmentFromTable(RegistryKey<LootTable> lootTable, LootContextParameterSet parameters, Map<EquipmentSlot, Float> slotDropChances) {
        this.setEquipmentFromTable(lootTable, parameters, 0L, slotDropChances);
    }

    default public void setEquipmentFromTable(RegistryKey<LootTable> lootTable, LootContextParameterSet parameters, long seed, Map<EquipmentSlot, Float> slotDropChances) {
        if (lootTable.equals(LootTables.EMPTY)) {
            return;
        }
        LootTable lootTable2 = parameters.getWorld().getServer().getReloadableRegistries().getLootTable(lootTable);
        if (lootTable2 == LootTable.EMPTY) {
            return;
        }
        ObjectArrayList<ItemStack> list = lootTable2.generateLoot(parameters, seed);
        ArrayList<EquipmentSlot> list2 = new ArrayList<EquipmentSlot>();
        for (ItemStack itemStack : list) {
            EquipmentSlot equipmentSlot = this.getSlotForStack(itemStack, list2);
            if (equipmentSlot == null) continue;
            ItemStack itemStack2 = equipmentSlot.split(itemStack);
            this.equipStack(equipmentSlot, itemStack2);
            Float float_ = slotDropChances.get(equipmentSlot);
            if (float_ != null) {
                this.setEquipmentDropChance(equipmentSlot, float_.floatValue());
            }
            list2.add(equipmentSlot);
        }
    }

    @Nullable
    default public EquipmentSlot getSlotForStack(ItemStack stack, List<EquipmentSlot> slotBlacklist) {
        if (stack.isEmpty()) {
            return null;
        }
        Equipment equipment = Equipment.fromStack(stack);
        if (equipment != null) {
            EquipmentSlot equipmentSlot = equipment.getSlotType();
            if (!slotBlacklist.contains(equipmentSlot)) {
                return equipmentSlot;
            }
        } else if (!slotBlacklist.contains(EquipmentSlot.MAINHAND)) {
            return EquipmentSlot.MAINHAND;
        }
        return null;
    }
}

