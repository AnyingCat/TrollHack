/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;

public class PlayerHeadBlockProfileFix
extends ChoiceFix {
    public PlayerHeadBlockProfileFix(Schema outputSchema) {
        super(outputSchema, false, "PlayerHeadBlockProfileFix", TypeReferences.BLOCK_ENTITY, "minecraft:skull");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fixProfile);
    }

    private <T> Dynamic<T> fixProfile(Dynamic<T> dynamic) {
        Optional<Dynamic<T>> optional2;
        Optional<Dynamic<T>> optional = dynamic.get("SkullOwner").result();
        Optional<Dynamic<T>> optional3 = optional.or(() -> PlayerHeadBlockProfileFix.method_58056(optional2 = dynamic.get("ExtraType").result()));
        if (optional3.isEmpty()) {
            return dynamic;
        }
        dynamic = dynamic.remove("SkullOwner").remove("ExtraType");
        dynamic = dynamic.set("profile", ItemStackComponentizationFix.createProfileDynamic(optional3.get()));
        return dynamic;
    }

    private static /* synthetic */ Optional method_58056(Optional optional) {
        return optional;
    }
}

