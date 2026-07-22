/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.datafixer.fix.TextFixes;

public class BlockEntitySignTextStrictJsonFix
extends ChoiceFix {
    public BlockEntitySignTextStrictJsonFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "BlockEntitySignTextStrictJsonFix", TypeReferences.BLOCK_ENTITY, "Sign");
    }

    private Dynamic<?> fix(Dynamic<?> signDynamic, String lineName) {
        return signDynamic.update(lineName, TextFixes::text);
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), linesDynamic -> {
            linesDynamic = this.fix((Dynamic<?>)linesDynamic, "Text1");
            linesDynamic = this.fix((Dynamic<?>)linesDynamic, "Text2");
            linesDynamic = this.fix((Dynamic<?>)linesDynamic, "Text3");
            linesDynamic = this.fix((Dynamic<?>)linesDynamic, "Text4");
            return linesDynamic;
        });
    }
}

