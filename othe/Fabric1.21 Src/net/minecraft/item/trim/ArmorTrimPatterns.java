/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item.trim;

import java.util.Optional;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ArmorTrimPatterns {
    public static final RegistryKey<ArmorTrimPattern> SENTRY = ArmorTrimPatterns.of("sentry");
    public static final RegistryKey<ArmorTrimPattern> DUNE = ArmorTrimPatterns.of("dune");
    public static final RegistryKey<ArmorTrimPattern> COAST = ArmorTrimPatterns.of("coast");
    public static final RegistryKey<ArmorTrimPattern> WILD = ArmorTrimPatterns.of("wild");
    public static final RegistryKey<ArmorTrimPattern> WARD = ArmorTrimPatterns.of("ward");
    public static final RegistryKey<ArmorTrimPattern> EYE = ArmorTrimPatterns.of("eye");
    public static final RegistryKey<ArmorTrimPattern> VEX = ArmorTrimPatterns.of("vex");
    public static final RegistryKey<ArmorTrimPattern> TIDE = ArmorTrimPatterns.of("tide");
    public static final RegistryKey<ArmorTrimPattern> SNOUT = ArmorTrimPatterns.of("snout");
    public static final RegistryKey<ArmorTrimPattern> RIB = ArmorTrimPatterns.of("rib");
    public static final RegistryKey<ArmorTrimPattern> SPIRE = ArmorTrimPatterns.of("spire");
    public static final RegistryKey<ArmorTrimPattern> WAYFINDER = ArmorTrimPatterns.of("wayfinder");
    public static final RegistryKey<ArmorTrimPattern> SHAPER = ArmorTrimPatterns.of("shaper");
    public static final RegistryKey<ArmorTrimPattern> SILENCE = ArmorTrimPatterns.of("silence");
    public static final RegistryKey<ArmorTrimPattern> RAISER = ArmorTrimPatterns.of("raiser");
    public static final RegistryKey<ArmorTrimPattern> HOST = ArmorTrimPatterns.of("host");
    public static final RegistryKey<ArmorTrimPattern> FLOW = ArmorTrimPatterns.of("flow");
    public static final RegistryKey<ArmorTrimPattern> BOLT = ArmorTrimPatterns.of("bolt");

    public static void bootstrap(Registerable<ArmorTrimPattern> registry) {
        ArmorTrimPatterns.register(registry, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, SENTRY);
        ArmorTrimPatterns.register(registry, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, DUNE);
        ArmorTrimPatterns.register(registry, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, COAST);
        ArmorTrimPatterns.register(registry, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, WILD);
        ArmorTrimPatterns.register(registry, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, WARD);
        ArmorTrimPatterns.register(registry, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, EYE);
        ArmorTrimPatterns.register(registry, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, VEX);
        ArmorTrimPatterns.register(registry, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, TIDE);
        ArmorTrimPatterns.register(registry, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, SNOUT);
        ArmorTrimPatterns.register(registry, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, RIB);
        ArmorTrimPatterns.register(registry, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, SPIRE);
        ArmorTrimPatterns.register(registry, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, WAYFINDER);
        ArmorTrimPatterns.register(registry, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, SHAPER);
        ArmorTrimPatterns.register(registry, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, SILENCE);
        ArmorTrimPatterns.register(registry, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, RAISER);
        ArmorTrimPatterns.register(registry, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, HOST);
        ArmorTrimPatterns.register(registry, Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, FLOW);
        ArmorTrimPatterns.register(registry, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, BOLT);
    }

    public static Optional<RegistryEntry.Reference<ArmorTrimPattern>> get(RegistryWrapper.WrapperLookup registriesLookup, ItemStack stack) {
        return registriesLookup.getWrapperOrThrow(RegistryKeys.TRIM_PATTERN).streamEntries().filter(pattern -> stack.itemMatches(((ArmorTrimPattern)pattern.value()).templateItem())).findFirst();
    }

    public static void register(Registerable<ArmorTrimPattern> registry, Item template, RegistryKey<ArmorTrimPattern> key) {
        ArmorTrimPattern armorTrimPattern = new ArmorTrimPattern(key.getValue(), Registries.ITEM.getEntry(template), Text.translatable(Util.createTranslationKey("trim_pattern", key.getValue())), false);
        registry.register(key, armorTrimPattern);
    }

    private static RegistryKey<ArmorTrimPattern> of(String id) {
        return RegistryKey.of(RegistryKeys.TRIM_PATTERN, Identifier.ofVanilla(id));
    }
}

