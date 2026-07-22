/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public interface EntitySubPredicate {
    public static final Codec<EntitySubPredicate> CODEC = Registries.ENTITY_SUB_PREDICATE_TYPE.getCodec().dispatch(EntitySubPredicate::getCodec, Function.identity());

    public MapCodec<? extends EntitySubPredicate> getCodec();

    public boolean test(Entity var1, ServerWorld var2, @Nullable Vec3d var3);
}

