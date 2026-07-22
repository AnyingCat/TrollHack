/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.decoration.painting;

import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class PaintingVariants {
    public static final RegistryKey<PaintingVariant> KEBAB = PaintingVariants.of("kebab");
    public static final RegistryKey<PaintingVariant> AZTEC = PaintingVariants.of("aztec");
    public static final RegistryKey<PaintingVariant> ALBAN = PaintingVariants.of("alban");
    public static final RegistryKey<PaintingVariant> AZTEC2 = PaintingVariants.of("aztec2");
    public static final RegistryKey<PaintingVariant> BOMB = PaintingVariants.of("bomb");
    public static final RegistryKey<PaintingVariant> PLANT = PaintingVariants.of("plant");
    public static final RegistryKey<PaintingVariant> WASTELAND = PaintingVariants.of("wasteland");
    public static final RegistryKey<PaintingVariant> POOL = PaintingVariants.of("pool");
    public static final RegistryKey<PaintingVariant> COURBET = PaintingVariants.of("courbet");
    public static final RegistryKey<PaintingVariant> SEA = PaintingVariants.of("sea");
    public static final RegistryKey<PaintingVariant> SUNSET = PaintingVariants.of("sunset");
    public static final RegistryKey<PaintingVariant> CREEBET = PaintingVariants.of("creebet");
    public static final RegistryKey<PaintingVariant> WANDERER = PaintingVariants.of("wanderer");
    public static final RegistryKey<PaintingVariant> GRAHAM = PaintingVariants.of("graham");
    public static final RegistryKey<PaintingVariant> MATCH = PaintingVariants.of("match");
    public static final RegistryKey<PaintingVariant> BUST = PaintingVariants.of("bust");
    public static final RegistryKey<PaintingVariant> STAGE = PaintingVariants.of("stage");
    public static final RegistryKey<PaintingVariant> VOID = PaintingVariants.of("void");
    public static final RegistryKey<PaintingVariant> SKULL_AND_ROSES = PaintingVariants.of("skull_and_roses");
    public static final RegistryKey<PaintingVariant> WITHER = PaintingVariants.of("wither");
    public static final RegistryKey<PaintingVariant> FIGHTERS = PaintingVariants.of("fighters");
    public static final RegistryKey<PaintingVariant> POINTER = PaintingVariants.of("pointer");
    public static final RegistryKey<PaintingVariant> PIGSCENE = PaintingVariants.of("pigscene");
    public static final RegistryKey<PaintingVariant> BURNING_SKULL = PaintingVariants.of("burning_skull");
    public static final RegistryKey<PaintingVariant> SKELETON = PaintingVariants.of("skeleton");
    public static final RegistryKey<PaintingVariant> DONKEY_KONG = PaintingVariants.of("donkey_kong");
    public static final RegistryKey<PaintingVariant> EARTH = PaintingVariants.of("earth");
    public static final RegistryKey<PaintingVariant> WIND = PaintingVariants.of("wind");
    public static final RegistryKey<PaintingVariant> WATER = PaintingVariants.of("water");
    public static final RegistryKey<PaintingVariant> FIRE = PaintingVariants.of("fire");
    public static final RegistryKey<PaintingVariant> BAROQUE = PaintingVariants.of("baroque");
    public static final RegistryKey<PaintingVariant> HUMBLE = PaintingVariants.of("humble");
    public static final RegistryKey<PaintingVariant> MEDITATIVE = PaintingVariants.of("meditative");
    public static final RegistryKey<PaintingVariant> PRAIRIE_RIDE = PaintingVariants.of("prairie_ride");
    public static final RegistryKey<PaintingVariant> UNPACKED = PaintingVariants.of("unpacked");
    public static final RegistryKey<PaintingVariant> BACKYARD = PaintingVariants.of("backyard");
    public static final RegistryKey<PaintingVariant> BOUQUET = PaintingVariants.of("bouquet");
    public static final RegistryKey<PaintingVariant> CAVEBIRD = PaintingVariants.of("cavebird");
    public static final RegistryKey<PaintingVariant> CHANGING = PaintingVariants.of("changing");
    public static final RegistryKey<PaintingVariant> COTAN = PaintingVariants.of("cotan");
    public static final RegistryKey<PaintingVariant> ENDBOSS = PaintingVariants.of("endboss");
    public static final RegistryKey<PaintingVariant> FERN = PaintingVariants.of("fern");
    public static final RegistryKey<PaintingVariant> FINDING = PaintingVariants.of("finding");
    public static final RegistryKey<PaintingVariant> LOWMIST = PaintingVariants.of("lowmist");
    public static final RegistryKey<PaintingVariant> ORB = PaintingVariants.of("orb");
    public static final RegistryKey<PaintingVariant> OWLEMONS = PaintingVariants.of("owlemons");
    public static final RegistryKey<PaintingVariant> PASSAGE = PaintingVariants.of("passage");
    public static final RegistryKey<PaintingVariant> POND = PaintingVariants.of("pond");
    public static final RegistryKey<PaintingVariant> SUNFLOWERS = PaintingVariants.of("sunflowers");
    public static final RegistryKey<PaintingVariant> TIDES = PaintingVariants.of("tides");

    public static void bootstrap(Registerable<PaintingVariant> registry) {
        PaintingVariants.register(registry, KEBAB, 1, 1);
        PaintingVariants.register(registry, AZTEC, 1, 1);
        PaintingVariants.register(registry, ALBAN, 1, 1);
        PaintingVariants.register(registry, AZTEC2, 1, 1);
        PaintingVariants.register(registry, BOMB, 1, 1);
        PaintingVariants.register(registry, PLANT, 1, 1);
        PaintingVariants.register(registry, WASTELAND, 1, 1);
        PaintingVariants.register(registry, POOL, 2, 1);
        PaintingVariants.register(registry, COURBET, 2, 1);
        PaintingVariants.register(registry, SEA, 2, 1);
        PaintingVariants.register(registry, SUNSET, 2, 1);
        PaintingVariants.register(registry, CREEBET, 2, 1);
        PaintingVariants.register(registry, WANDERER, 1, 2);
        PaintingVariants.register(registry, GRAHAM, 1, 2);
        PaintingVariants.register(registry, MATCH, 2, 2);
        PaintingVariants.register(registry, BUST, 2, 2);
        PaintingVariants.register(registry, STAGE, 2, 2);
        PaintingVariants.register(registry, VOID, 2, 2);
        PaintingVariants.register(registry, SKULL_AND_ROSES, 2, 2);
        PaintingVariants.register(registry, WITHER, 2, 2);
        PaintingVariants.register(registry, FIGHTERS, 4, 2);
        PaintingVariants.register(registry, POINTER, 4, 4);
        PaintingVariants.register(registry, PIGSCENE, 4, 4);
        PaintingVariants.register(registry, BURNING_SKULL, 4, 4);
        PaintingVariants.register(registry, SKELETON, 4, 3);
        PaintingVariants.register(registry, EARTH, 2, 2);
        PaintingVariants.register(registry, WIND, 2, 2);
        PaintingVariants.register(registry, WATER, 2, 2);
        PaintingVariants.register(registry, FIRE, 2, 2);
        PaintingVariants.register(registry, DONKEY_KONG, 4, 3);
        PaintingVariants.register(registry, BAROQUE, 2, 2);
        PaintingVariants.register(registry, HUMBLE, 2, 2);
        PaintingVariants.register(registry, MEDITATIVE, 1, 1);
        PaintingVariants.register(registry, PRAIRIE_RIDE, 1, 2);
        PaintingVariants.register(registry, UNPACKED, 4, 4);
        PaintingVariants.register(registry, BACKYARD, 3, 4);
        PaintingVariants.register(registry, BOUQUET, 3, 3);
        PaintingVariants.register(registry, CAVEBIRD, 3, 3);
        PaintingVariants.register(registry, CHANGING, 4, 2);
        PaintingVariants.register(registry, COTAN, 3, 3);
        PaintingVariants.register(registry, ENDBOSS, 3, 3);
        PaintingVariants.register(registry, FERN, 3, 3);
        PaintingVariants.register(registry, FINDING, 4, 2);
        PaintingVariants.register(registry, LOWMIST, 4, 2);
        PaintingVariants.register(registry, ORB, 4, 4);
        PaintingVariants.register(registry, OWLEMONS, 3, 3);
        PaintingVariants.register(registry, PASSAGE, 4, 2);
        PaintingVariants.register(registry, POND, 3, 4);
        PaintingVariants.register(registry, SUNFLOWERS, 3, 3);
        PaintingVariants.register(registry, TIDES, 3, 3);
    }

    private static void register(Registerable<PaintingVariant> registry, RegistryKey<PaintingVariant> key, int width, int height) {
        registry.register(key, new PaintingVariant(width, height, key.getValue()));
    }

    private static RegistryKey<PaintingVariant> of(String id) {
        return RegistryKey.of(RegistryKeys.PAINTING_VARIANT, Identifier.ofVanilla(id));
    }
}

