/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.slf4j.Logger;

public record ReferenceLootCondition(RegistryKey<LootCondition> id) implements LootCondition
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<ReferenceLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryKey.createCodec(RegistryKeys.PREDICATE).fieldOf("name")).forGetter(ReferenceLootCondition::id)).apply((Applicative<ReferenceLootCondition, ?>)instance, ReferenceLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.REFERENCE;
    }

    @Override
    public void validate(LootTableReporter reporter) {
        if (!reporter.canUseReferences()) {
            reporter.report("Uses reference to " + String.valueOf(this.id.getValue()) + ", but references are not allowed");
            return;
        }
        if (reporter.isInStack(this.id)) {
            reporter.report("Condition " + String.valueOf(this.id.getValue()) + " is recursively called");
            return;
        }
        LootCondition.super.validate(reporter);
        reporter.getDataLookup().getOptionalEntry(RegistryKeys.PREDICATE, this.id).ifPresentOrElse(entry -> ((LootCondition)entry.value()).validate(reporter.makeChild(".{" + String.valueOf(this.id.getValue()) + "}", this.id)), () -> reporter.report("Unknown condition table called " + String.valueOf(this.id.getValue())));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean test(LootContext lootContext) {
        LootCondition lootCondition = lootContext.getLookup().getOptionalEntry(RegistryKeys.PREDICATE, this.id).map(RegistryEntry.Reference::value).orElse(null);
        if (lootCondition == null) {
            LOGGER.warn("Tried using unknown condition table called {}", (Object)this.id.getValue());
            return false;
        }
        LootContext.Entry<LootCondition> entry = LootContext.predicate(lootCondition);
        if (lootContext.markActive(entry)) {
            try {
                boolean bl = lootCondition.test(lootContext);
                return bl;
            } finally {
                lootContext.markInactive(entry);
            }
        }
        LOGGER.warn("Detected infinite loop in loot tables");
        return false;
    }

    public static LootCondition.Builder builder(RegistryKey<LootCondition> key) {
        return () -> new ReferenceLootCondition(key);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

