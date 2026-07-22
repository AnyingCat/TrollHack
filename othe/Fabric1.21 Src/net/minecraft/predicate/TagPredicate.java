/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

public record TagPredicate<T>(TagKey<T> tag, boolean expected) {
    public static <T> Codec<TagPredicate<T>> createCodec(RegistryKey<? extends Registry<T>> registryRef) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)TagKey.unprefixedCodec(registryRef).fieldOf("id")).forGetter(TagPredicate::tag), ((MapCodec)Codec.BOOL.fieldOf("expected")).forGetter(TagPredicate::expected)).apply((Applicative<TagPredicate, ?>)instance, TagPredicate::new));
    }

    public static <T> TagPredicate<T> expected(TagKey<T> tag) {
        return new TagPredicate<T>(tag, true);
    }

    public static <T> TagPredicate<T> unexpected(TagKey<T> tag) {
        return new TagPredicate<T>(tag, false);
    }

    public boolean test(RegistryEntry<T> registryEntry) {
        return registryEntry.isIn(this.tag) == this.expected;
    }
}

