/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item.trim;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ArmorTrim
implements TooltipAppender {
    public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ArmorTrimMaterial.ENTRY_CODEC.fieldOf("material")).forGetter(ArmorTrim::getMaterial), ((MapCodec)ArmorTrimPattern.ENTRY_CODEC.fieldOf("pattern")).forGetter(ArmorTrim::getPattern), Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(trim -> trim.showInTooltip)).apply((Applicative<ArmorTrim, ?>)instance, ArmorTrim::new));
    public static final PacketCodec<RegistryByteBuf, ArmorTrim> PACKET_CODEC = PacketCodec.tuple(ArmorTrimMaterial.ENTRY_PACKET_CODEC, ArmorTrim::getMaterial, ArmorTrimPattern.ENTRY_PACKET_CODEC, ArmorTrim::getPattern, PacketCodecs.BOOL, trim -> trim.showInTooltip, ArmorTrim::new);
    private static final Text UPGRADE_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.upgrade"))).formatted(Formatting.GRAY);
    private final RegistryEntry<ArmorTrimMaterial> material;
    private final RegistryEntry<ArmorTrimPattern> pattern;
    private final boolean showInTooltip;
    private final Function<RegistryEntry<ArmorMaterial>, Identifier> leggingsModelIdGetter;
    private final Function<RegistryEntry<ArmorMaterial>, Identifier> genericModelIdGetter;

    private ArmorTrim(RegistryEntry<ArmorTrimMaterial> material, RegistryEntry<ArmorTrimPattern> pattern, boolean showInTooltip, Function<RegistryEntry<ArmorMaterial>, Identifier> leggingsModelIdGetter, Function<RegistryEntry<ArmorMaterial>, Identifier> genericModelIdGetter) {
        this.material = material;
        this.pattern = pattern;
        this.showInTooltip = showInTooltip;
        this.leggingsModelIdGetter = leggingsModelIdGetter;
        this.genericModelIdGetter = genericModelIdGetter;
    }

    public ArmorTrim(RegistryEntry<ArmorTrimMaterial> material, RegistryEntry<ArmorTrimPattern> pattern, boolean showInTooltip) {
        this.material = material;
        this.pattern = pattern;
        this.leggingsModelIdGetter = Util.memoize(materialEntry -> {
            Identifier identifier = ((ArmorTrimPattern)pattern.value()).assetId();
            String string = ArmorTrim.getMaterialAssetNameFor(material, materialEntry);
            return identifier.withPath(materialName -> "trims/models/armor/" + materialName + "_leggings_" + string);
        });
        this.genericModelIdGetter = Util.memoize(materialEntry -> {
            Identifier identifier = ((ArmorTrimPattern)pattern.value()).assetId();
            String string = ArmorTrim.getMaterialAssetNameFor(material, materialEntry);
            return identifier.withPath(materialName -> "trims/models/armor/" + materialName + "_" + string);
        });
        this.showInTooltip = showInTooltip;
    }

    public ArmorTrim(RegistryEntry<ArmorTrimMaterial> material, RegistryEntry<ArmorTrimPattern> pattern) {
        this(material, pattern, true);
    }

    private static String getMaterialAssetNameFor(RegistryEntry<ArmorTrimMaterial> material, RegistryEntry<ArmorMaterial> armorMaterial) {
        Map<RegistryEntry<ArmorMaterial>, String> map = material.value().overrideArmorMaterials();
        String string = map.get(armorMaterial);
        if (string != null) {
            return string;
        }
        return material.value().assetName();
    }

    public boolean equals(RegistryEntry<ArmorTrimPattern> pattern, RegistryEntry<ArmorTrimMaterial> material) {
        return pattern.equals(this.pattern) && material.equals(this.material);
    }

    public RegistryEntry<ArmorTrimPattern> getPattern() {
        return this.pattern;
    }

    public RegistryEntry<ArmorTrimMaterial> getMaterial() {
        return this.material;
    }

    public Identifier getLeggingsModelId(RegistryEntry<ArmorMaterial> armorMaterial) {
        return this.leggingsModelIdGetter.apply(armorMaterial);
    }

    public Identifier getGenericModelId(RegistryEntry<ArmorMaterial> armorMaterial) {
        return this.genericModelIdGetter.apply(armorMaterial);
    }

    public boolean equals(Object o) {
        if (o instanceof ArmorTrim) {
            ArmorTrim armorTrim = (ArmorTrim)o;
            return this.showInTooltip == armorTrim.showInTooltip && this.pattern.equals(armorTrim.pattern) && this.material.equals(armorTrim.material);
        }
        return false;
    }

    public int hashCode() {
        int i = this.material.hashCode();
        i = 31 * i + this.pattern.hashCode();
        i = 31 * i + (this.showInTooltip ? 1 : 0);
        return i;
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        if (!this.showInTooltip) {
            return;
        }
        tooltip.accept(UPGRADE_TEXT);
        tooltip.accept(ScreenTexts.space().append(this.pattern.value().getDescription(this.material)));
        tooltip.accept(ScreenTexts.space().append(this.material.value().description()));
    }

    public ArmorTrim withShowInTooltip(boolean showInTooltip) {
        return new ArmorTrim(this.material, this.pattern, showInTooltip, this.leggingsModelIdGetter, this.genericModelIdGetter);
    }
}

