/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.HungerConstants;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;

/**
 * Represents the components that make up the properties of a food item.
 */
public record FoodComponent(int nutrition, float saturation, boolean canAlwaysEat, float eatSeconds, Optional<ItemStack> usingConvertsTo, List<StatusEffectEntry> effects) {
    private static final float DEFAULT_EAT_SECONDS = 1.6f;
    public static final Codec<FoodComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codecs.NONNEGATIVE_INT.fieldOf("nutrition")).forGetter(FoodComponent::nutrition), ((MapCodec)Codec.FLOAT.fieldOf("saturation")).forGetter(FoodComponent::saturation), Codec.BOOL.optionalFieldOf("can_always_eat", false).forGetter(FoodComponent::canAlwaysEat), Codecs.POSITIVE_FLOAT.optionalFieldOf("eat_seconds", Float.valueOf(1.6f)).forGetter(FoodComponent::eatSeconds), ItemStack.UNCOUNTED_CODEC.optionalFieldOf("using_converts_to").forGetter(FoodComponent::usingConvertsTo), StatusEffectEntry.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(FoodComponent::effects)).apply((Applicative<FoodComponent, ?>)instance, FoodComponent::new));
    public static final PacketCodec<RegistryByteBuf, FoodComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, FoodComponent::nutrition, PacketCodecs.FLOAT, FoodComponent::saturation, PacketCodecs.BOOL, FoodComponent::canAlwaysEat, PacketCodecs.FLOAT, FoodComponent::eatSeconds, ItemStack.PACKET_CODEC.collect(PacketCodecs::optional), FoodComponent::usingConvertsTo, StatusEffectEntry.PACKET_CODEC.collect(PacketCodecs.toList()), FoodComponent::effects, FoodComponent::new);

    public int getEatTicks() {
        return (int)(this.eatSeconds * 20.0f);
    }

    public record StatusEffectEntry(StatusEffectInstance effect, float probability) {
        private final StatusEffectInstance effect;
        public static final Codec<StatusEffectEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)StatusEffectInstance.CODEC.fieldOf("effect")).forGetter(StatusEffectEntry::effect), Codec.floatRange(0.0f, 1.0f).optionalFieldOf("probability", Float.valueOf(1.0f)).forGetter(StatusEffectEntry::probability)).apply((Applicative<StatusEffectEntry, ?>)instance, StatusEffectEntry::new));
        public static final PacketCodec<RegistryByteBuf, StatusEffectEntry> PACKET_CODEC = PacketCodec.tuple(StatusEffectInstance.PACKET_CODEC, StatusEffectEntry::effect, PacketCodecs.FLOAT, StatusEffectEntry::probability, StatusEffectEntry::new);

        public StatusEffectInstance effect() {
            return new StatusEffectInstance(this.effect);
        }
    }

    public static class Builder {
        private int nutrition;
        private float saturationModifier;
        private boolean canAlwaysEat;
        private float eatSeconds = 1.6f;
        private Optional<ItemStack> usingConvertsTo = Optional.empty();
        private final ImmutableList.Builder<StatusEffectEntry> effects = ImmutableList.builder();

        public Builder nutrition(int nutrition) {
            this.nutrition = nutrition;
            return this;
        }

        public Builder saturationModifier(float saturationModifier) {
            this.saturationModifier = saturationModifier;
            return this;
        }

        public Builder alwaysEdible() {
            this.canAlwaysEat = true;
            return this;
        }

        public Builder snack() {
            this.eatSeconds = 0.8f;
            return this;
        }

        public Builder statusEffect(StatusEffectInstance effect, float chance) {
            this.effects.add((Object)new StatusEffectEntry(effect, chance));
            return this;
        }

        public Builder usingConvertsTo(ItemConvertible item) {
            this.usingConvertsTo = Optional.of(new ItemStack(item));
            return this;
        }

        public FoodComponent build() {
            float f = HungerConstants.calculateSaturation(this.nutrition, this.saturationModifier);
            return new FoodComponent(this.nutrition, f, this.canAlwaysEat, this.eatSeconds, this.usingConvertsTo, (List<StatusEffectEntry>)((Object)this.effects.build()));
        }
    }
}

