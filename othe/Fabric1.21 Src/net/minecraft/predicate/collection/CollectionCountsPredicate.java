/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.collection;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.predicate.NumberRange;

public interface CollectionCountsPredicate<T, P extends Predicate<T>>
extends Predicate<Iterable<T>> {
    public List<Entry<T, P>> getEntries();

    public static <T, P extends Predicate<T>> Codec<CollectionCountsPredicate<T, P>> createCodec(Codec<P> predicateCodec) {
        return Entry.createCodec(predicateCodec).listOf().xmap(CollectionCountsPredicate::create, CollectionCountsPredicate::getEntries);
    }

    @SafeVarargs
    public static <T, P extends Predicate<T>> CollectionCountsPredicate<T, P> create(Entry<T, P> ... entries) {
        return CollectionCountsPredicate.create(List.of(entries));
    }

    public static <T, P extends Predicate<T>> CollectionCountsPredicate<T, P> create(List<Entry<T, P>> entries) {
        return switch (entries.size()) {
            case 0 -> new Empty();
            case 1 -> new Single<T, P>(entries.getFirst());
            default -> new Multiple<T, P>(entries);
        };
    }

    public record Entry<T, P extends Predicate<T>>(P test, NumberRange.IntRange count) {
        public static <T, P extends Predicate<T>> Codec<Entry<T, P>> createCodec(Codec<P> predicateCodec) {
            return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)predicateCodec.fieldOf("test")).forGetter(Entry::test), ((MapCodec)NumberRange.IntRange.CODEC.fieldOf("count")).forGetter(Entry::count)).apply((Applicative<Entry, ?>)instance, Entry::new));
        }

        public boolean test(Iterable<T> collection) {
            int i = 0;
            for (T object : collection) {
                if (!this.test.test(object)) continue;
                ++i;
            }
            return this.count.test(i);
        }
    }

    public static class Empty<T, P extends Predicate<T>>
    implements CollectionCountsPredicate<T, P> {
        @Override
        public boolean test(Iterable<T> iterable) {
            return true;
        }

        @Override
        public List<Entry<T, P>> getEntries() {
            return List.of();
        }

        @Override
        public /* synthetic */ boolean test(Object collection) {
            return this.test((Iterable)collection);
        }
    }

    public record Single<T, P extends Predicate<T>>(Entry<T, P> entry) implements CollectionCountsPredicate<T, P>
    {
        @Override
        public boolean test(Iterable<T> iterable) {
            return this.entry.test(iterable);
        }

        @Override
        public List<Entry<T, P>> getEntries() {
            return List.of(this.entry);
        }

        @Override
        public /* synthetic */ boolean test(Object collection) {
            return this.test((Iterable)collection);
        }
    }

    public record Multiple<T, P extends Predicate<T>>(List<Entry<T, P>> entries) implements CollectionCountsPredicate<T, P>
    {
        @Override
        public boolean test(Iterable<T> iterable) {
            for (Entry<T, P> entry : this.entries) {
                if (entry.test(iterable)) continue;
                return false;
            }
            return true;
        }

        @Override
        public List<Entry<T, P>> getEntries() {
            return this.entries;
        }

        @Override
        public /* synthetic */ boolean test(Object collection) {
            return this.test((Iterable)collection);
        }
    }
}

