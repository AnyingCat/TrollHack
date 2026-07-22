/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class Schema3938
extends IdentifierNormalizingSchema {
    public Schema3938(int i, Schema schema) {
        super(i, schema);
    }

    protected static TypeTemplate method_59913(Schema schema) {
        return DSL.optionalFields("inBlockState", TypeReferences.BLOCK_STATE.in(schema), "item", TypeReferences.ITEM_STACK.in(schema), "weapon", TypeReferences.ITEM_STACK.in(schema));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        schema.register(map, "minecraft:spectral_arrow", () -> Schema3938.method_59913(schema));
        schema.register(map, "minecraft:arrow", () -> Schema3938.method_59913(schema));
        return map;
    }
}

