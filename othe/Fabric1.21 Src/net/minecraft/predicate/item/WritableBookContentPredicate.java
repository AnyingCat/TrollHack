/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.collection.CollectionPredicate;
import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.text.RawFilteredPair;

public record WritableBookContentPredicate(Optional<CollectionPredicate<RawFilteredPair<String>, RawStringPredicate>> pages) implements ComponentSubPredicate<WritableBookContentComponent>
{
    public static final Codec<WritableBookContentPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(CollectionPredicate.createCodec(RawStringPredicate.CODEC).optionalFieldOf("pages").forGetter(WritableBookContentPredicate::pages)).apply((Applicative<WritableBookContentPredicate, ?>)instance, WritableBookContentPredicate::new));

    @Override
    public ComponentType<WritableBookContentComponent> getComponentType() {
        return DataComponentTypes.WRITABLE_BOOK_CONTENT;
    }

    @Override
    public boolean test(ItemStack itemStack, WritableBookContentComponent writableBookContentComponent) {
        return !this.pages.isPresent() || this.pages.get().test(writableBookContentComponent.pages());
    }

    public record RawStringPredicate(String contents) implements Predicate<RawFilteredPair<String>>
    {
        public static final Codec<RawStringPredicate> CODEC = Codec.STRING.xmap(RawStringPredicate::new, RawStringPredicate::contents);

        @Override
        public boolean test(RawFilteredPair<String> rawFilteredPair) {
            return rawFilteredPair.raw().equals(this.contents);
        }

        @Override
        public /* synthetic */ boolean test(Object string) {
            return this.test((RawFilteredPair)string);
        }
    }
}

