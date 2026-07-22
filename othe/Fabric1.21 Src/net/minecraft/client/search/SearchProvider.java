/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.search;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.search.SuffixArray;

/**
 * A functional interface that provides searching.
 */
@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface SearchProvider<T> {
    public static <T> SearchProvider<T> empty() {
        return string -> List.of();
    }

    public static <T> SearchProvider<T> plainText(List<T> list, Function<T, Stream<String>> function) {
        if (list.isEmpty()) {
            return SearchProvider.empty();
        }
        SuffixArray suffixArray = new SuffixArray();
        for (Object object : list) {
            function.apply(object).forEach(string -> suffixArray.add(object, string.toLowerCase(Locale.ROOT)));
        }
        suffixArray.build();
        return suffixArray::findAll;
    }

    /**
     * {@return the search result of {@code text}}
     */
    public List<T> findAll(String var1);
}

