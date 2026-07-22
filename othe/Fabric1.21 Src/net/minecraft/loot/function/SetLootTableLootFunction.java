/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerLootComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

public class SetLootTableLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetLootTableLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetLootTableLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).fieldOf("name")).forGetter(function -> function.lootTable), Codec.LONG.optionalFieldOf("seed", 0L).forGetter(function -> function.seed), ((MapCodec)Registries.BLOCK_ENTITY_TYPE.getEntryCodec().fieldOf("type")).forGetter(function -> function.type))).apply((Applicative<SetLootTableLootFunction, ?>)instance, SetLootTableLootFunction::new));
    private final RegistryKey<LootTable> lootTable;
    private final long seed;
    private final RegistryEntry<BlockEntityType<?>> type;

    private SetLootTableLootFunction(List<LootCondition> conditions, RegistryKey<LootTable> lootTable, long seed, RegistryEntry<BlockEntityType<?>> blockEntityType) {
        super(conditions);
        this.lootTable = lootTable;
        this.seed = seed;
        this.type = blockEntityType;
    }

    public LootFunctionType<SetLootTableLootFunction> getType() {
        return LootFunctionTypes.SET_LOOT_TABLE;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (stack.isEmpty()) {
            return stack;
        }
        stack.set(DataComponentTypes.CONTAINER_LOOT, new ContainerLootComponent(this.lootTable, this.seed));
        return stack;
    }

    @Override
    public void validate(LootTableReporter reporter) {
        super.validate(reporter);
        if (!reporter.canUseReferences()) {
            reporter.report("Uses reference to " + String.valueOf(this.lootTable.getValue()) + ", but references are not allowed");
            return;
        }
        if (reporter.getDataLookup().getOptionalEntry(RegistryKeys.LOOT_TABLE, this.lootTable).isEmpty()) {
            reporter.report("Missing loot table used for container: " + String.valueOf(this.lootTable.getValue()));
        }
    }

    public static ConditionalLootFunction.Builder<?> builder(BlockEntityType<?> type, RegistryKey<LootTable> lootTable) {
        return SetLootTableLootFunction.builder(conditions -> new SetLootTableLootFunction((List<LootCondition>)conditions, lootTable, 0L, (RegistryEntry<BlockEntityType<?>>)type.getRegistryEntry()));
    }

    public static ConditionalLootFunction.Builder<?> builder(BlockEntityType<?> type, RegistryKey<LootTable> lootTable, long seed) {
        return SetLootTableLootFunction.builder(conditions -> new SetLootTableLootFunction((List<LootCondition>)conditions, lootTable, seed, (RegistryEntry<BlockEntityType<?>>)type.getRegistryEntry()));
    }
}

