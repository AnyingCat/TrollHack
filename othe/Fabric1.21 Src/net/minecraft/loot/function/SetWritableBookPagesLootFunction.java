/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.util.collection.ListOperation;

public class SetWritableBookPagesLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetWritableBookPagesLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetWritableBookPagesLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)WritableBookContentComponent.PAGES_CODEC.fieldOf("pages")).forGetter(function -> function.pages), ListOperation.createCodec(100).forGetter(function -> function.operation))).apply((Applicative<SetWritableBookPagesLootFunction, ?>)instance, SetWritableBookPagesLootFunction::new));
    private final List<RawFilteredPair<String>> pages;
    private final ListOperation operation;

    protected SetWritableBookPagesLootFunction(List<LootCondition> conditions, List<RawFilteredPair<String>> pages, ListOperation operation) {
        super(conditions);
        this.pages = pages;
        this.operation = operation;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        stack.apply(DataComponentTypes.WRITABLE_BOOK_CONTENT, WritableBookContentComponent.DEFAULT, this::apply);
        return stack;
    }

    public WritableBookContentComponent apply(WritableBookContentComponent current) {
        List<RawFilteredPair<String>> list = this.operation.apply(current.pages(), this.pages, 100);
        return current.withPages((List)list);
    }

    public LootFunctionType<SetWritableBookPagesLootFunction> getType() {
        return LootFunctionTypes.SET_WRITABLE_BOOK_PAGES;
    }
}

