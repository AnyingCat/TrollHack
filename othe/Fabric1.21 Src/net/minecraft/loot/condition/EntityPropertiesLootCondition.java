/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.util.math.Vec3d;

public record EntityPropertiesLootCondition(Optional<EntityPredicate> predicate, LootContext.EntityTarget entity) implements LootCondition
{
    public static final MapCodec<EntityPropertiesLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(EntityPredicate.CODEC.optionalFieldOf("predicate").forGetter(EntityPropertiesLootCondition::predicate), ((MapCodec)LootContext.EntityTarget.CODEC.fieldOf("entity")).forGetter(EntityPropertiesLootCondition::entity)).apply((Applicative<EntityPropertiesLootCondition, ?>)instance, EntityPropertiesLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.ENTITY_PROPERTIES;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.ORIGIN, this.entity.getParameter());
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.get(this.entity.getParameter());
        Vec3d vec3d = lootContext.get(LootContextParameters.ORIGIN);
        return this.predicate.isEmpty() || this.predicate.get().test(lootContext.getWorld(), vec3d, entity);
    }

    public static LootCondition.Builder create(LootContext.EntityTarget entity) {
        return EntityPropertiesLootCondition.builder(entity, EntityPredicate.Builder.create());
    }

    public static LootCondition.Builder builder(LootContext.EntityTarget entity, EntityPredicate.Builder predicateBuilder) {
        return () -> new EntityPropertiesLootCondition(Optional.of(predicateBuilder.build()), entity);
    }

    public static LootCondition.Builder builder(LootContext.EntityTarget entity, EntityPredicate predicate) {
        return () -> new EntityPropertiesLootCondition(Optional.of(predicate), entity);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

