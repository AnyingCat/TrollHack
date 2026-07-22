/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.datafixer.fix.TextFixes;

public class DropInvalidSignDatafixDataFix
extends ChoiceFix {
    private static final String[] KEYS_TO_REMOVE = new String[]{"Text1", "Text2", "Text3", "Text4", "FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4", "Color", "GlowingText"};

    public DropInvalidSignDatafixDataFix(Schema outputSchema, String name, String blockEntityId) {
        super(outputSchema, false, name, TypeReferences.BLOCK_ENTITY, blockEntityId);
    }

    private static <T> Dynamic<T> dropInvalidDatafixData(Dynamic<T> blockEntityData) {
        blockEntityData = blockEntityData.update("front_text", DropInvalidSignDatafixDataFix::dropInvalidDatafixDataOnSide);
        blockEntityData = blockEntityData.update("back_text", DropInvalidSignDatafixDataFix::dropInvalidDatafixDataOnSide);
        for (String string : KEYS_TO_REMOVE) {
            blockEntityData = blockEntityData.remove(string);
        }
        return blockEntityData;
    }

    private static <T> Dynamic<T> dropInvalidDatafixDataOnSide(Dynamic<T> textData) {
        boolean bl = textData.get("_filtered_correct").asBoolean(false);
        if (bl) {
            return textData.remove("_filtered_correct");
        }
        Optional<Stream<Dynamic<T>>> optional = textData.get("filtered_messages").asStreamOpt().result();
        if (optional.isEmpty()) {
            return textData;
        }
        Dynamic dynamic = TextFixes.empty(textData.getOps());
        List<Dynamic> list = textData.get("messages").asStreamOpt().result().orElse(Stream.of(new Dynamic[0])).toList();
        List<Dynamic> list2 = Streams.mapWithIndex(optional.get(), (message, index) -> {
            Dynamic dynamic2 = index < (long)list.size() ? (Dynamic)list.get((int)index) : dynamic;
            return message.equals(dynamic) ? dynamic2 : message;
        }).toList();
        if (list2.stream().allMatch(message -> message.equals(dynamic))) {
            return textData.remove("filtered_messages");
        }
        return textData.set("filtered_messages", textData.createList(list2.stream()));
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), DropInvalidSignDatafixDataFix::dropInvalidDatafixData);
    }
}

