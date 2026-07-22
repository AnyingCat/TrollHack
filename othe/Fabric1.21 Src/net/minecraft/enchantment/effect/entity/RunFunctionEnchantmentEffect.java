/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;

public record RunFunctionEnchantmentEffect(Identifier function) implements EnchantmentEntityEffect
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<RunFunctionEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("function")).forGetter(RunFunctionEnchantmentEffect::function)).apply((Applicative<RunFunctionEnchantmentEffect, ?>)instance, RunFunctionEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        MinecraftServer minecraftServer = world.getServer();
        CommandFunctionManager commandFunctionManager = minecraftServer.getCommandFunctionManager();
        Optional<CommandFunction<ServerCommandSource>> optional = commandFunctionManager.getFunction(this.function);
        if (optional.isPresent()) {
            ServerCommandSource serverCommandSource = minecraftServer.getCommandSource().withLevel(2).withSilent().withEntity(user).withWorld(world).withPosition(pos).withRotation(user.getRotationClient());
            commandFunctionManager.execute(optional.get(), serverCommandSource);
        } else {
            LOGGER.error("Enchantment run_function effect failed for non-existent function {}", (Object)this.function);
        }
    }

    public MapCodec<RunFunctionEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

