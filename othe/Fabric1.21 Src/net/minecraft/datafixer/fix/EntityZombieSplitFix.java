/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.EntityTransformFix;
import net.minecraft.util.Util;

public class EntityZombieSplitFix
extends EntityTransformFix {
    private final Supplier<Type<?>> ZOMBIE_VILLAGER_TYPE = Suppliers.memoize(() -> this.getOutputSchema().getChoiceType(TypeReferences.ENTITY, "ZombieVillager"));

    public EntityZombieSplitFix(Schema outputSchema) {
        super("EntityZombieSplitFix", outputSchema, true);
    }

    @Override
    protected Pair<String, Typed<?>> transform(String choice, Typed<?> entityTyped) {
        String string;
        if (!choice.equals("Zombie")) {
            return Pair.of(choice, entityTyped);
        }
        Dynamic<?> dynamic = entityTyped.getOptional(DSL.remainderFinder()).orElseThrow();
        int i = dynamic.get("ZombieType").asInt(0);
        return Pair.of(string, (switch (i) {
            default -> {
                string = "Zombie";
                yield entityTyped;
            }
            case 1, 2, 3, 4, 5 -> {
                string = "ZombieVillager";
                yield this.setZombieVillagerProfession(entityTyped, i - 1);
            }
            case 6 -> {
                string = "Husk";
                yield entityTyped;
            }
        }).update(DSL.remainderFinder(), entityDynamic -> entityDynamic.remove("ZombieType")));
    }

    private Typed<?> setZombieVillagerProfession(Typed<?> entityTyped, int variant) {
        return Util.apply(entityTyped, this.ZOMBIE_VILLAGER_TYPE.get(), zombieVillagerDynamic -> zombieVillagerDynamic.set("Profession", zombieVillagerDynamic.createInt(variant)));
    }
}

