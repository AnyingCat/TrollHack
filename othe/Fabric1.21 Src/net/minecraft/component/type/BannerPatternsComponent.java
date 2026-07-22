/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.slf4j.Logger;

public record BannerPatternsComponent(List<Layer> layers) {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final BannerPatternsComponent DEFAULT = new BannerPatternsComponent(List.of());
    public static final Codec<BannerPatternsComponent> CODEC = Layer.CODEC.listOf().xmap(BannerPatternsComponent::new, BannerPatternsComponent::layers);
    public static final PacketCodec<RegistryByteBuf, BannerPatternsComponent> PACKET_CODEC = Layer.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(BannerPatternsComponent::new, BannerPatternsComponent::layers);

    public BannerPatternsComponent withoutTopLayer() {
        return new BannerPatternsComponent(List.copyOf(this.layers.subList(0, this.layers.size() - 1)));
    }

    public record Layer(RegistryEntry<BannerPattern> pattern, DyeColor color) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BannerPattern.ENTRY_CODEC.fieldOf("pattern")).forGetter(Layer::pattern), ((MapCodec)DyeColor.CODEC.fieldOf("color")).forGetter(Layer::color)).apply((Applicative<Layer, ?>)instance, Layer::new));
        public static final PacketCodec<RegistryByteBuf, Layer> PACKET_CODEC = PacketCodec.tuple(BannerPattern.ENTRY_PACKET_CODEC, Layer::pattern, DyeColor.PACKET_CODEC, Layer::color, Layer::new);

        public MutableText getTooltipText() {
            String string = this.pattern.value().translationKey();
            return Text.translatable(string + "." + this.color.getName());
        }
    }

    public static class Builder {
        private final ImmutableList.Builder<Layer> entries = ImmutableList.builder();

        @Deprecated
        public Builder add(RegistryEntryLookup<BannerPattern> patternLookup, RegistryKey<BannerPattern> pattern, DyeColor color) {
            Optional<RegistryEntry.Reference<BannerPattern>> optional = patternLookup.getOptional(pattern);
            if (optional.isEmpty()) {
                LOGGER.warn("Unable to find banner pattern with id: '{}'", (Object)pattern.getValue());
                return this;
            }
            return this.add((RegistryEntry<BannerPattern>)optional.get(), color);
        }

        public Builder add(RegistryEntry<BannerPattern> pattern, DyeColor color) {
            return this.add(new Layer(pattern, color));
        }

        public Builder add(Layer layer) {
            this.entries.add((Object)layer);
            return this;
        }

        public Builder addAll(BannerPatternsComponent patterns) {
            this.entries.addAll(patterns.layers);
            return this;
        }

        public BannerPatternsComponent build() {
            return new BannerPatternsComponent((List<Layer>)((Object)this.entries.build()));
        }
    }
}

