/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.rule.blockentity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.rule.blockentity.RuleBlockEntityModifier;
import net.minecraft.structure.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AppendLootRuleBlockEntityModifier
implements RuleBlockEntityModifier {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<AppendLootRuleBlockEntityModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).fieldOf("loot_table")).forGetter(modifier -> modifier.lootTable)).apply((Applicative<AppendLootRuleBlockEntityModifier, ?>)instance, AppendLootRuleBlockEntityModifier::new));
    private final RegistryKey<LootTable> lootTable;

    public AppendLootRuleBlockEntityModifier(RegistryKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    public NbtCompound modifyBlockEntityNbt(Random random, @Nullable NbtCompound nbt) {
        NbtCompound nbtCompound = nbt == null ? new NbtCompound() : nbt.copy();
        RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).encodeStart(NbtOps.INSTANCE, this.lootTable).resultOrPartial(LOGGER::error).ifPresent(nbtx -> nbtCompound.put("LootTable", (NbtElement)nbtx));
        nbtCompound.putLong("LootTableSeed", random.nextLong());
        return nbtCompound;
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.APPEND_LOOT;
    }
}

