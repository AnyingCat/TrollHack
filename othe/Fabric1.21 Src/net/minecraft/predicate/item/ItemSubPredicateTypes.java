/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.item;

import com.mojang.serialization.Codec;
import net.minecraft.predicate.item.AttributeModifiersPredicate;
import net.minecraft.predicate.item.BundleContentsPredicate;
import net.minecraft.predicate.item.ContainerPredicate;
import net.minecraft.predicate.item.CustomDataPredicate;
import net.minecraft.predicate.item.DamagePredicate;
import net.minecraft.predicate.item.EnchantmentsPredicate;
import net.minecraft.predicate.item.FireworkExplosionPredicate;
import net.minecraft.predicate.item.FireworksPredicate;
import net.minecraft.predicate.item.ItemSubPredicate;
import net.minecraft.predicate.item.JukeboxPlayablePredicate;
import net.minecraft.predicate.item.PotionContentsPredicate;
import net.minecraft.predicate.item.TrimPredicate;
import net.minecraft.predicate.item.WritableBookContentPredicate;
import net.minecraft.predicate.item.WrittenBookContentPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ItemSubPredicateTypes {
    public static final ItemSubPredicate.Type<DamagePredicate> DAMAGE = ItemSubPredicateTypes.register("damage", DamagePredicate.CODEC);
    public static final ItemSubPredicate.Type<EnchantmentsPredicate.Enchantments> ENCHANTMENTS = ItemSubPredicateTypes.register("enchantments", EnchantmentsPredicate.Enchantments.CODEC);
    public static final ItemSubPredicate.Type<EnchantmentsPredicate.StoredEnchantments> STORED_ENCHANTMENTS = ItemSubPredicateTypes.register("stored_enchantments", EnchantmentsPredicate.StoredEnchantments.CODEC);
    public static final ItemSubPredicate.Type<PotionContentsPredicate> POTION_CONTENTS = ItemSubPredicateTypes.register("potion_contents", PotionContentsPredicate.CODEC);
    public static final ItemSubPredicate.Type<CustomDataPredicate> CUSTOM_DATA = ItemSubPredicateTypes.register("custom_data", CustomDataPredicate.CODEC);
    public static final ItemSubPredicate.Type<ContainerPredicate> CONTAINER = ItemSubPredicateTypes.register("container", ContainerPredicate.CODEC);
    public static final ItemSubPredicate.Type<BundleContentsPredicate> BUNDLE_CONTENTS = ItemSubPredicateTypes.register("bundle_contents", BundleContentsPredicate.CODEC);
    public static final ItemSubPredicate.Type<FireworkExplosionPredicate> FIREWORK_EXPLOSION = ItemSubPredicateTypes.register("firework_explosion", FireworkExplosionPredicate.CODEC);
    public static final ItemSubPredicate.Type<FireworksPredicate> FIREWORKS = ItemSubPredicateTypes.register("fireworks", FireworksPredicate.CODEC);
    public static final ItemSubPredicate.Type<WritableBookContentPredicate> WRITABLE_BOOK_CONTENT = ItemSubPredicateTypes.register("writable_book_content", WritableBookContentPredicate.CODEC);
    public static final ItemSubPredicate.Type<WrittenBookContentPredicate> WRITTEN_BOOK_CONTENT = ItemSubPredicateTypes.register("written_book_content", WrittenBookContentPredicate.CODEC);
    public static final ItemSubPredicate.Type<AttributeModifiersPredicate> ATTRIBUTE_MODIFIERS = ItemSubPredicateTypes.register("attribute_modifiers", AttributeModifiersPredicate.CODEC);
    public static final ItemSubPredicate.Type<TrimPredicate> TRIM = ItemSubPredicateTypes.register("trim", TrimPredicate.CODEC);
    public static final ItemSubPredicate.Type<JukeboxPlayablePredicate> JUKEBOX_PLAYABLE = ItemSubPredicateTypes.register("jukebox_playable", JukeboxPlayablePredicate.CODEC);

    private static <T extends ItemSubPredicate> ItemSubPredicate.Type<T> register(String id, Codec<T> codec) {
        return Registry.register(Registries.ITEM_SUB_PREDICATE_TYPE, id, new ItemSubPredicate.Type<T>(codec));
    }

    public static ItemSubPredicate.Type<?> getDefault(Registry<ItemSubPredicate.Type<?>> registry) {
        return DAMAGE;
    }
}

