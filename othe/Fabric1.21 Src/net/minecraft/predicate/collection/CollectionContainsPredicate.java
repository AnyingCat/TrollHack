/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.collection;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface CollectionContainsPredicate<T, P extends Predicate<T>>
extends Predicate<Iterable<T>> {
    public List<P> getPredicates();

    public static <T, P extends Predicate<T>> Codec<CollectionContainsPredicate<T, P>> createCodec(Codec<P> predicateCodec) {
        return predicateCodec.listOf().xmap(CollectionContainsPredicate::create, CollectionContainsPredicate::getPredicates);
    }

    @SafeVarargs
    public static <T, P extends Predicate<T>> CollectionContainsPredicate<T, P> create(P ... predicates) {
        return CollectionContainsPredicate.create(List.of(predicates));
    }

    public static <T, P extends Predicate<T>> CollectionContainsPredicate<T, P> create(List<P> predicates) {
        return switch (predicates.size()) {
            case 0 -> new Empty();
            case 1 -> new Single((Predicate)predicates.getFirst());
            default -> new Multiple(predicates);
        };
    }

    public static class Empty<T, P extends Predicate<T>>
    implements CollectionContainsPredicate<T, P> {
        @Override
        public boolean test(Iterable<T> iterable) {
            return true;
        }

        @Override
        public List<P> getPredicates() {
            return List.of();
        }

        @Override
        public /* synthetic */ boolean test(Object collection) {
            return this.test((Iterable)collection);
        }
    }

    public record Single<T, P extends Predicate<T>>(P test) implements CollectionContainsPredicate<T, P>
    {
        @Override
        public boolean test(Iterable<T> iterable) {
            for (T object : iterable) {
                if (!this.test.test(object)) continue;
                return true;
            }
            return false;
        }

        @Override
        public List<P> getPredicates() {
            return List.of(this.test);
        }

        @Override
        public /* synthetic */ boolean test(Object collection) {
            return this.test((Iterable)collection);
        }
    }

    public record Multiple<T, P extends Predicate<T>>(List<P> tests) implements CollectionContainsPredicate<T, P>
    {
        @Override
        public boolean test(Iterable<T> iterable) {
            ArrayList<P> list = new ArrayList<P>(this.tests);
            for (Object object : iterable) {
                list.removeIf(predicate -> predicate.test(object));
                if (!list.isEmpty()) continue;
                return true;
            }
            return false;
        }

        @Override
        public List<P> getPredicates() {
            return this.tests;
        }

        @Override
        public /* synthetic */ boolean test(Object collection) {
            return this.test((Iterable)collection);
        }
    }
}

