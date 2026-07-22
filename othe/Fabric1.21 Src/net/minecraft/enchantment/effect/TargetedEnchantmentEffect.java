/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextType;

public record TargetedEnchantmentEffect<T>(EnchantmentEffectTarget enchanted, EnchantmentEffectTarget affected, T effect, Optional<LootCondition> requirements) {
    public static <S> Codec<TargetedEnchantmentEffect<S>> createPostAttackCodec(Codec<S> effectCodec, LootContextType lootContextType) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)EnchantmentEffectTarget.CODEC.fieldOf("enchanted")).forGetter(TargetedEnchantmentEffect::enchanted), ((MapCodec)EnchantmentEffectTarget.CODEC.fieldOf("affected")).forGetter(TargetedEnchantmentEffect::affected), ((MapCodec)effectCodec.fieldOf("effect")).forGetter(TargetedEnchantmentEffect::effect), EnchantmentEffectEntry.createRequirementsCodec(lootContextType).optionalFieldOf("requirements").forGetter(TargetedEnchantmentEffect::requirements)).apply((Applicative<TargetedEnchantmentEffect, ?>)instance, TargetedEnchantmentEffect::new));
    }

    public static <S> Codec<TargetedEnchantmentEffect<S>> createEquipmentDropsCodec(Codec<S> effectCodec, LootContextType lootContextType) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)EnchantmentEffectTarget.CODEC.validate(enchanted -> enchanted != EnchantmentEffectTarget.DAMAGING_ENTITY ? DataResult.success(enchanted) : DataResult.error(() -> "enchanted must be attacker or victim")).fieldOf("enchanted")).forGetter(TargetedEnchantmentEffect::enchanted), ((MapCodec)effectCodec.fieldOf("effect")).forGetter(TargetedEnchantmentEffect::effect), EnchantmentEffectEntry.createRequirementsCodec(lootContextType).optionalFieldOf("requirements").forGetter(TargetedEnchantmentEffect::requirements)).apply((Applicative<TargetedEnchantmentEffect, ?>)instance, (enchantedx, effect, requirements) -> new TargetedEnchantmentEffect<Object>((EnchantmentEffectTarget)enchantedx, EnchantmentEffectTarget.VICTIM, effect, (Optional<LootCondition>)requirements)));
    }

    public boolean test(LootContext lootContext) {
        if (this.requirements.isEmpty()) {
            return true;
        }
        return this.requirements.get().test(lootContext);
    }
}

