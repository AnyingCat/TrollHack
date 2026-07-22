/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.math.MathHelper;

public class VillagerXpRebuildFix
extends DataFix {
    private static final int TRADES_PER_LEVEL = 2;
    private static final int[] LEVEL_TO_XP = new int[]{0, 10, 50, 100, 150};

    public static int levelToXp(int level) {
        return LEVEL_TO_XP[MathHelper.clamp(level - 1, 0, LEVEL_TO_XP.length - 1)];
    }

    public VillagerXpRebuildFix(Schema outputSchema, boolean changesTyped) {
        super(outputSchema, changesTyped);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getChoiceType(TypeReferences.ENTITY, "minecraft:villager");
        OpticFinder<?> opticFinder = DSL.namedChoice("minecraft:villager", type);
        OpticFinder<?> opticFinder2 = type.findField("Offers");
        Type<?> type2 = opticFinder2.type();
        OpticFinder<?> opticFinder3 = type2.findField("Recipes");
        List.ListType listType = (List.ListType)opticFinder3.type();
        OpticFinder opticFinder4 = listType.getElement().finder();
        return this.fixTypeEverywhereTyped("Villager level and xp rebuild", this.getInputSchema().getType(TypeReferences.ENTITY), entityTyped -> entityTyped.updateTyped(opticFinder, type, villagerTyped -> {
            Optional<Number> optional;
            int j;
            Dynamic<?> dynamic = villagerTyped.get(DSL.remainderFinder());
            int i = dynamic.get("VillagerData").get("level").asInt(0);
            Typed<?> typed = villagerTyped;
            if ((i == 0 || i == 1) && (i = MathHelper.clamp((j = villagerTyped.getOptionalTyped(opticFinder2).flatMap(offersTyped -> offersTyped.getOptionalTyped(opticFinder3)).map(recipesTyped -> recipesTyped.getAllTyped(opticFinder4).size()).orElse(0).intValue()) / 2, 1, 5)) > 1) {
                typed = VillagerXpRebuildFix.fixLevel(typed, i);
            }
            if ((optional = dynamic.get("Xp").asNumber().result()).isEmpty()) {
                typed = VillagerXpRebuildFix.fixXp(typed, i);
            }
            return typed;
        }));
    }

    private static Typed<?> fixLevel(Typed<?> villagerTyped, int level) {
        return villagerTyped.update(DSL.remainderFinder(), villagerdynamic -> villagerdynamic.update("VillagerData", villagerDataDynamic -> villagerDataDynamic.set("level", villagerDataDynamic.createInt(level))));
    }

    private static Typed<?> fixXp(Typed<?> villagerTyped, int level) {
        int i = VillagerXpRebuildFix.levelToXp(level);
        return villagerTyped.update(DSL.remainderFinder(), villagerDynamic -> villagerDynamic.set("Xp", villagerDynamic.createInt(i)));
    }
}

