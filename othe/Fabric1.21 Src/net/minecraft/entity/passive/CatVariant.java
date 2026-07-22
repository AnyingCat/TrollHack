/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public record CatVariant(Identifier texture) {
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<CatVariant>> PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.CAT_VARIANT);
    public static final RegistryKey<CatVariant> TABBY = CatVariant.of("tabby");
    public static final RegistryKey<CatVariant> BLACK = CatVariant.of("black");
    public static final RegistryKey<CatVariant> RED = CatVariant.of("red");
    public static final RegistryKey<CatVariant> SIAMESE = CatVariant.of("siamese");
    public static final RegistryKey<CatVariant> BRITISH_SHORTHAIR = CatVariant.of("british_shorthair");
    public static final RegistryKey<CatVariant> CALICO = CatVariant.of("calico");
    public static final RegistryKey<CatVariant> PERSIAN = CatVariant.of("persian");
    public static final RegistryKey<CatVariant> RAGDOLL = CatVariant.of("ragdoll");
    public static final RegistryKey<CatVariant> WHITE = CatVariant.of("white");
    public static final RegistryKey<CatVariant> JELLIE = CatVariant.of("jellie");
    public static final RegistryKey<CatVariant> ALL_BLACK = CatVariant.of("all_black");

    private static RegistryKey<CatVariant> of(String id) {
        return RegistryKey.of(RegistryKeys.CAT_VARIANT, Identifier.ofVanilla(id));
    }

    public static CatVariant registerAndGetDefault(Registry<CatVariant> registry) {
        CatVariant.register(registry, TABBY, "textures/entity/cat/tabby.png");
        CatVariant.register(registry, BLACK, "textures/entity/cat/black.png");
        CatVariant.register(registry, RED, "textures/entity/cat/red.png");
        CatVariant.register(registry, SIAMESE, "textures/entity/cat/siamese.png");
        CatVariant.register(registry, BRITISH_SHORTHAIR, "textures/entity/cat/british_shorthair.png");
        CatVariant.register(registry, CALICO, "textures/entity/cat/calico.png");
        CatVariant.register(registry, PERSIAN, "textures/entity/cat/persian.png");
        CatVariant.register(registry, RAGDOLL, "textures/entity/cat/ragdoll.png");
        CatVariant.register(registry, WHITE, "textures/entity/cat/white.png");
        CatVariant.register(registry, JELLIE, "textures/entity/cat/jellie.png");
        return CatVariant.register(registry, ALL_BLACK, "textures/entity/cat/all_black.png");
    }

    private static CatVariant register(Registry<CatVariant> registry, RegistryKey<CatVariant> key, String textureId) {
        return Registry.register(registry, key, new CatVariant(Identifier.ofVanilla(textureId)));
    }
}

