/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Util;

public class ProjectileItemTypeFix
extends DataFix {
    private static final String EMPTY_ID = "minecraft:empty";

    public ProjectileItemTypeFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.ENTITY);
        Type<?> type2 = this.getOutputSchema().getType(TypeReferences.ENTITY);
        return this.fixTypeEverywhereTyped("Fix AbstractArrow item type", type, type2, FixUtil.compose(this.createFixApplier("minecraft:trident", ProjectileItemTypeFix::fixTrident), this.createFixApplier("minecraft:arrow", ProjectileItemTypeFix::fixArrow), this.createFixApplier("minecraft:spectral_arrow", ProjectileItemTypeFix::fixSpectralArrow)));
    }

    private Function<Typed<?>, Typed<?>> createFixApplier(String id, Fixer<?> fixer) {
        Type<?> type = this.getInputSchema().getChoiceType(TypeReferences.ENTITY, id);
        Type<?> type2 = this.getOutputSchema().getChoiceType(TypeReferences.ENTITY, id);
        return ProjectileItemTypeFix.createFixApplier(id, fixer, type, type2);
    }

    private static <T> Function<Typed<?>, Typed<?>> createFixApplier(String id, Fixer<?> fixer, Type<?> inputType, Type<T> outputType) {
        OpticFinder<?> opticFinder = DSL.namedChoice(id, inputType);
        Fixer<?> fixer2 = fixer;
        return typed2 -> typed2.updateTyped(opticFinder, outputType, typed -> fixer2.fix((Typed<?>)typed, outputType));
    }

    private static <T> Typed<T> fixArrow(Typed<?> typed, Type<T> type) {
        return Util.apply(typed, type, data -> data.set("item", ProjectileItemTypeFix.createStack(data, ProjectileItemTypeFix.getArrowId(data))));
    }

    private static String getArrowId(Dynamic<?> arrowData) {
        return arrowData.get("Potion").asString(EMPTY_ID).equals(EMPTY_ID) ? "minecraft:arrow" : "minecraft:tipped_arrow";
    }

    private static <T> Typed<T> fixSpectralArrow(Typed<?> typed, Type<T> type) {
        return Util.apply(typed, type, data -> data.set("item", ProjectileItemTypeFix.createStack(data, "minecraft:spectral_arrow")));
    }

    private static Dynamic<?> createStack(Dynamic<?> projectileData, String id) {
        return projectileData.createMap(ImmutableMap.of(projectileData.createString("id"), projectileData.createString(id), projectileData.createString("Count"), projectileData.createInt(1)));
    }

    private static <T> Typed<T> fixTrident(Typed<?> typed, Type<T> type) {
        return new Typed<T>(type, typed.getOps(), typed.getValue());
    }

    static interface Fixer<F> {
        public Typed<F> fix(Typed<?> var1, Type<F> var2);
    }
}

