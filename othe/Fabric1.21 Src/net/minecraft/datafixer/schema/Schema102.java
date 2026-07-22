/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.Schema99;

public class Schema102
extends Schema {
    public Schema102(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        schema.registerType(true, TypeReferences.ITEM_STACK, () -> DSL.hook(DSL.optionalFields("id", TypeReferences.ITEM_NAME.in(schema), "tag", DSL.optionalFields(Pair.of("EntityTag", TypeReferences.ENTITY_TREE.in(schema)), Pair.of("BlockEntityTag", TypeReferences.BLOCK_ENTITY.in(schema)), Pair.of("CanDestroy", DSL.list(TypeReferences.BLOCK_NAME.in(schema))), Pair.of("CanPlaceOn", DSL.list(TypeReferences.BLOCK_NAME.in(schema))), Pair.of("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema))), Pair.of("ChargedProjectiles", DSL.list(TypeReferences.ITEM_STACK.in(schema))))), Schema99.field_5747, Hook.HookFunction.IDENTITY));
    }
}

