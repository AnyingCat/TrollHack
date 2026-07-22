/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContextAware;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public record LootDataType<T>(RegistryKey<Registry<T>> registryKey, Codec<T> codec, Validator<T> validator) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LootDataType<LootCondition> PREDICATES = new LootDataType<LootCondition>(RegistryKeys.PREDICATE, LootCondition.CODEC, LootDataType.simpleValidator());
    public static final LootDataType<LootFunction> ITEM_MODIFIERS = new LootDataType<LootFunction>(RegistryKeys.ITEM_MODIFIER, LootFunctionTypes.CODEC, LootDataType.simpleValidator());
    public static final LootDataType<LootTable> LOOT_TABLES = new LootDataType<LootTable>(RegistryKeys.LOOT_TABLE, LootTable.CODEC, LootDataType.tableValidator());

    public void validate(LootTableReporter reporter, RegistryKey<T> key, T value) {
        this.validator.run(reporter, key, value);
    }

    public <V> Optional<T> parse(Identifier id, DynamicOps<V> ops, V json) {
        DataResult dataResult = this.codec.parse(ops, json);
        dataResult.error().ifPresent(error -> LOGGER.error("Couldn't parse element {}/{} - {}", this.registryKey.getValue(), id, error.message()));
        return dataResult.result();
    }

    public static Stream<LootDataType<?>> stream() {
        return Stream.of(PREDICATES, ITEM_MODIFIERS, LOOT_TABLES);
    }

    private static <T extends LootContextAware> Validator<T> simpleValidator() {
        return (reporter, key, value) -> value.validate(reporter.makeChild("{" + String.valueOf(key.getRegistry()) + "/" + String.valueOf(key.getValue()) + "}", key));
    }

    private static Validator<LootTable> tableValidator() {
        return (reporter, key, value) -> value.validate(reporter.withContextType(value.getType()).makeChild("{" + String.valueOf(key.getRegistry()) + "/" + String.valueOf(key.getValue()) + "}", key));
    }

    @FunctionalInterface
    public static interface Validator<T> {
        public void run(LootTableReporter var1, RegistryKey<T> var2, T var3);
    }
}

