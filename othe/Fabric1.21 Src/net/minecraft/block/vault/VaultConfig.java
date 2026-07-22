/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.vault;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.spawner.EntityDetector;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public record VaultConfig(RegistryKey<LootTable> lootTable, double activationRange, double deactivationRange, ItemStack keyItem, Optional<RegistryKey<LootTable>> overrideLootTableToDisplay, EntityDetector playerDetector, EntityDetector.Selector entitySelector) {
    static final String CONFIG_KEY = "config";
    static VaultConfig DEFAULT = new VaultConfig();
    static Codec<VaultConfig> codec = RecordCodecBuilder.create(instance -> instance.group(RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).lenientOptionalFieldOf("loot_table", DEFAULT.lootTable()).forGetter(VaultConfig::lootTable), Codec.DOUBLE.lenientOptionalFieldOf("activation_range", DEFAULT.activationRange()).forGetter(VaultConfig::activationRange), Codec.DOUBLE.lenientOptionalFieldOf("deactivation_range", DEFAULT.deactivationRange()).forGetter(VaultConfig::deactivationRange), ItemStack.createOptionalCodec("key_item").forGetter(VaultConfig::keyItem), RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).lenientOptionalFieldOf("override_loot_table_to_display").forGetter(VaultConfig::overrideLootTableToDisplay)).apply((Applicative<VaultConfig, ?>)instance, VaultConfig::new)).validate(VaultConfig::validate);

    private VaultConfig() {
        this(LootTables.TRIAL_CHAMBERS_REWARD_CHEST, 4.0, 4.5, new ItemStack(Items.TRIAL_KEY), Optional.empty(), EntityDetector.NON_SPECTATOR_PLAYERS, EntityDetector.Selector.IN_WORLD);
    }

    public VaultConfig(RegistryKey<LootTable> lootTable, double activationRange, double deactivationRange, ItemStack keyItem, Optional<RegistryKey<LootTable>> overrideLootTableToDisplay) {
        this(lootTable, activationRange, deactivationRange, keyItem, overrideLootTableToDisplay, DEFAULT.playerDetector(), DEFAULT.entitySelector());
    }

    private DataResult<VaultConfig> validate() {
        if (this.activationRange > this.deactivationRange) {
            return DataResult.error(() -> "Activation range must (" + this.activationRange + ") be less or equal to deactivation range (" + this.deactivationRange + ")");
        }
        return DataResult.success(this);
    }
}

